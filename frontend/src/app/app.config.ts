import { provideHttpClient, withFetch, withInterceptors } from '@angular/common/http';
import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter, withComponentInputBinding, withHashLocation } from '@angular/router';
import { environment } from '../environments/environment';
import { routes } from './app.routes';
import { authInterceptor } from './core/auth-interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    // withComponentInputBinding passes the :ref from the URL into the detail page's ref input.
    // On GitHub Pages the route goes after a # (see environment.pages.ts).
    provideRouter(
      routes,
      withComponentInputBinding(),
      ...(environment.useHashRouting ? [withHashLocation()] : []),
    ),
    // authInterceptor adds the login token to every request to the backend
    provideHttpClient(withFetch(), withInterceptors([authInterceptor])),
  ],
};
