import { provideHttpClient, withFetch, withInterceptors } from '@angular/common/http';
import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { routes } from './app.routes';
import { authInterceptor } from './core/auth-interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    // withComponentInputBinding passes the :ref from the URL into the detail page's ref input
    provideRouter(routes, withComponentInputBinding()),
    // authInterceptor adds the login token to every request to the backend
    provideHttpClient(withFetch(), withInterceptors([authInterceptor])),
  ],
};
