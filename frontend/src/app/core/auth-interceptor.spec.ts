import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Router, UrlTree, provideRouter } from '@angular/router';
import { authGuard } from './auth-guard';
import { authInterceptor } from './auth-interceptor';
import { Auth } from './auth';
import { storeSession } from './test-data';

describe('authInterceptor and authGuard', () => {
  let http: HttpClient;
  let backend: HttpTestingController;

  function setUp(): void {
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
      ],
    });
    http = TestBed.inject(HttpClient);
    backend = TestBed.inject(HttpTestingController);
  }

  beforeEach(() => sessionStorage.clear());
  afterEach(() => backend.verify());

  it('adds the token to requests to the backend', () => {
    storeSession();
    setUp();

    http.get('http://localhost:8080/api/applications').subscribe();

    const request = backend.expectOne('http://localhost:8080/api/applications');
    expect(request.request.headers.get('Authorization')).toBe('Bearer test-token');
    request.flush({});
  });

  it('does not send the token anywhere else', () => {
    storeSession();
    setUp();

    http.get('https://example.com/data').subscribe();

    const request = backend.expectOne('https://example.com/data');
    expect(request.request.headers.has('Authorization')).toBe(false);
    request.flush({});
  });

  it('logs out when the backend says 401', () => {
    storeSession();
    setUp();
    const navigate = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);

    http.get('http://localhost:8080/api/applications').subscribe({ error: () => undefined });
    backend
      .expectOne('http://localhost:8080/api/applications')
      .flush(null, { status: 401, statusText: 'Unauthorized' });

    expect(TestBed.inject(Auth).isLoggedIn()).toBe(false);
    expect(navigate).toHaveBeenCalledWith(['/login'], { queryParams: { reason: 'expired' } });
  });

  it('guard sends visitors who are not logged in to the login page', () => {
    setUp();

    const result = TestBed.runInInjectionContext(() =>
      authGuard({} as never, { url: '/applications/SNM-2026-000001' } as never),
    );

    expect(result instanceof UrlTree).toBe(true);
    expect(TestBed.inject(Router).serializeUrl(result as UrlTree)).toBe(
      '/login?returnUrl=%2Fapplications%2FSNM-2026-000001',
    );
  });

  it('guard lets logged-in reviewers through', () => {
    storeSession();
    setUp();

    const result = TestBed.runInInjectionContext(() =>
      authGuard({} as never, { url: '/applications' } as never),
    );

    expect(result).toBe(true);
  });
});
