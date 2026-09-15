import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
  {
    path: 'dashboard',
    title: 'Dashboard | OpsPilot',
    loadComponent: () =>
      import('./features/dashboard/dashboard.component').then(
        (component) => component.DashboardComponent,
      ),
  },
  {
    path: 'accounts/:accountId',
    title: 'Account | OpsPilot',
    loadComponent: () =>
      import('./features/accounts/account-detail.component').then(
        (component) => component.AccountDetailComponent,
      ),
  },
  {
    path: '**',
    title: 'Not Found | OpsPilot',
    loadComponent: () =>
      import('./features/not-found/not-found.component').then(
        (component) => component.NotFoundComponent,
      ),
  },
];
