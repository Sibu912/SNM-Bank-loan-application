import { Component, inject, input, signal } from '@angular/core';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { Auth } from '../core/auth';

/**
 * Reviewer login. The username and password are checked by the backend against the
 * reviewer table in PostgreSQL; on success we keep the returned JWT and open the admin.
 */
@Component({
  selector: 'app-login',
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login {
  private readonly auth = inject(Auth);
  private readonly router = inject(Router);

  /** From the URL: where to go after logging in, and why we were sent here. */
  readonly returnUrl = input<string>();
  readonly reason = input<string>();

  protected readonly loanFormUrl = 'https://sibu912.github.io/SNM-Bank-loan-application/';

  protected readonly username = signal('');
  protected readonly password = signal('');
  protected readonly showPassword = signal(false);
  protected readonly busy = signal(false);
  protected readonly error = signal<string | null>(null);

  protected submit(event: Event): void {
    event.preventDefault();
    if (!this.username().trim() || !this.password()) {
      this.error.set('ENTER YOUR USERNAME AND PASSWORD');
      return;
    }

    this.busy.set(true);
    this.error.set(null);
    this.auth.login(this.username().trim(), this.password()).subscribe({
      next: () => {
        this.busy.set(false);
        this.router.navigateByUrl(this.safeReturnUrl());
      },
      error: (error: unknown) => {
        this.busy.set(false);
        this.password.set('');
        this.error.set(
          error instanceof HttpErrorResponse && error.status === 0
            ? 'CONNECTION LOST // IS THE LOAN SERVICE RUNNING?'
            : 'ACCESS DENIED // WRONG USERNAME OR PASSWORD',
        );
      },
    });
  }

  /** Only allow paths inside this app, never a full URL to another site. */
  private safeReturnUrl(): string {
    const url = this.returnUrl();
    return url && url.startsWith('/') && !url.startsWith('//') ? url : '/applications';
  }
}
