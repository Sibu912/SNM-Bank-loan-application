import { Routes } from '@angular/router';
import { ApplicationDetail } from './applications/application-detail/application-detail';
import { ApplicationList } from './applications/application-list/application-list';
import { authGuard } from './core/auth-guard';
import { Login } from './login/login';

export const routes: Routes = [
  { path: '', redirectTo: 'applications', pathMatch: 'full' },
  { path: 'login', component: Login, title: 'Login · SNM Bank' },
  {
    path: 'applications',
    component: ApplicationList,
    canActivate: [authGuard],
    title: 'Applications · SNM Bank',
  },
  {
    path: 'applications/:ref',
    component: ApplicationDetail,
    canActivate: [authGuard],
    title: 'Application · SNM Bank',
  },
  { path: '**', redirectTo: 'applications' },
];
