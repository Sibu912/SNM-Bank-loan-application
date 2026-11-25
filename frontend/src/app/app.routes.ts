import { Routes } from '@angular/router';
import { ApplicationDetail } from './applications/application-detail/application-detail';
import { ApplicationList } from './applications/application-list/application-list';

export const routes: Routes = [
  { path: '', redirectTo: 'applications', pathMatch: 'full' },
  { path: 'applications', component: ApplicationList, title: 'Applications · SNM Bank' },
  { path: 'applications/:ref', component: ApplicationDetail, title: 'Application · SNM Bank' },
  { path: '**', redirectTo: 'applications' },
];
