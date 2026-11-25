import { HttpClient } from '@angular/common/http';
import { Service, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, map } from 'rxjs';
import { environment } from '../../environments/environment';
import { LoginResponse } from './models';

const STORAGE_KEY = 'snm_admin_session';

/**
 * Who is logged in. The JWT from the backend is kept in sessionStorage,
 * so a page refresh keeps you logged in, but closing the tab logs you out.
 */
@Service()
export class Auth {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  private readonly session = signal<LoginResponse | null>(readStoredSession());

  readonly user = computed(() => this.session());
  readonly isLoggedIn = computed(() => this.session() !== null);

  login(username: string, password: string): Observable<void> {
    return this.http
      .post<LoginResponse>(`${environment.apiUrl}/api/auth/login`, { username, password })
      .pipe(
        map((response) => {
          sessionStorage.setItem(STORAGE_KEY, JSON.stringify(response));
          this.session.set(response);
        }),
      );
  }

  /** The token to send as "Authorization: Bearer ...", or null if it is missing or expired. */
  token(): string | null {
    const session = this.session();
    if (!session) {
      return null;
    }
    if (isExpired(session)) {
      this.clear();
      return null;
    }
    return session.token;
  }

  logout(reason?: 'expired'): void {
    this.clear();
    this.router.navigate(['/login'], reason ? { queryParams: { reason } } : {});
  }

  private clear(): void {
    sessionStorage.removeItem(STORAGE_KEY);
    this.session.set(null);
  }
}

function readStoredSession(): LoginResponse | null {
  try {
    const stored = sessionStorage.getItem(STORAGE_KEY);
    const session = stored ? (JSON.parse(stored) as LoginResponse) : null;
    return session && !isExpired(session) ? session : null;
  } catch {
    return null;
  }
}

function isExpired(session: LoginResponse): boolean {
  return new Date(session.expiresAt).getTime() <= Date.now();
}
