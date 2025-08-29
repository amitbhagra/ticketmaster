import { Routes } from '@angular/router';
import { Dashboard } from './dashboard/dashboard';
import { EventsList } from './events-list/events-list';
import { MyBookings } from './my-bookings/my-bookings';

export const routes: Routes = [
  {
    path: 'dashboard',
    component: Dashboard,
    children: [
      { path: 'events', component: EventsList },
      { path: 'bookings', component: MyBookings },
      { path: '', redirectTo: 'events', pathMatch: 'full' }
    ]
  },
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' }
];
