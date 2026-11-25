import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { Auth } from './auth';
import { loginResponse, storeSession } from './test-data';

describe('Auth', () => {
  function setUp(): { auth: Auth; http: HttpTestingController } {
    TestBed.configureTestingModule({
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    });
    return { auth: TestBed.inject(Auth), http: TestBed.inject(HttpTestingController) };
  }

  beforeEach(() => sessionStorage.clear());

  it('logs in and keeps the token', () => {
    const { auth, http } = setUp();

    auth.login('thandi', 'Reviewer#2026').subscribe();
    const request = http.expectOne('http://localhost:8080/api/auth/login');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({ username: 'thandi', password: 'Reviewer#2026' });
    request.flush(loginResponse());

    expect(auth.isLoggedIn()).toBe(true);
    expect(auth.token()).toBe('test-token');
    expect(auth.user()?.displayName).toBe('Thandi Mokoena');
    expect(sessionStorage.getItem('snm_admin_session')).toContain('test-token');
  });

  it('remembers a login after a page refresh', () => {
    storeSession();
    const { auth } = setUp();

    expect(auth.isLoggedIn()).toBe(true);
  });

  it('ignores an expired login', () => {
    storeSession(loginResponse({ expiresAt: new Date(Date.now() - 1000).toISOString() }));
    const { auth } = setUp();

    expect(auth.isLoggedIn()).toBe(false);
    expect(auth.token()).toBeNull();
  });

  it('logs out and goes to the login page', () => {
    storeSession();
    const { auth } = setUp();
    const navigate = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);

    auth.logout();

    expect(auth.isLoggedIn()).toBe(false);
    expect(sessionStorage.getItem('snm_admin_session')).toBeNull();
    expect(navigate).toHaveBeenCalledWith(['/login'], {});
  });
});
