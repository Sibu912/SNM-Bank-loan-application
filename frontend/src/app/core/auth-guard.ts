import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { Auth } from './auth';

/** Admin pages need a login. Otherwise go to /login and come back here afterwards. */
export const authGuard: CanActivateFn = (_route, state) => {
  const auth = inject(Auth);
  if (auth.token()) {
    return true;
  }
  return inject(Router).createUrlTree(['/login'], { queryParams: { returnUrl: state.url } });
};
