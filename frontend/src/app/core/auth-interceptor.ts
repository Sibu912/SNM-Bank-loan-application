import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { environment } from '../../environments/environment';
import { Auth } from './auth';

/**
 * Adds "Authorization: Bearer <token>" to every request to our backend,
 * and sends the user back to the login page if the backend answers 401.
 */
export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const auth = inject(Auth);
  const isApiCall = request.url.startsWith(environment.apiUrl);
  const isLoginCall = request.url.endsWith('/api/auth/login');
  const token = isApiCall && !isLoginCall ? auth.token() : null;

  const authorised = token
    ? request.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : request;

  return next(authorised).pipe(
    catchError((error: unknown) => {
      if (error instanceof HttpErrorResponse && error.status === 401 && isApiCall && !isLoginCall) {
        auth.logout('expired');
      }
      return throwError(() => error);
    }),
  );
};
