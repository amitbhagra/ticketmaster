import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: 'dashboard',
    loadComponent: () => import('./dashboard/dashboard').then((m) => m.Dashboard),
    children: [
      {
        path: 'events',
        loadComponent: () => import('./events-list/events-list').then((m) => m.EventsList),
      },
      {
        path: 'bookings',
        loadComponent: () => import('./my-bookings/my-bookings').then((m) => m.MyBookings),
      },
      { path: '', redirectTo: 'events', pathMatch: 'full' }
    ]
  },
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' }
];
