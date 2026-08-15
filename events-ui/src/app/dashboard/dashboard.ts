import { Component, inject, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { MenuItem } from 'primeng/api';
import { PanelMenu } from 'primeng/panelmenu';
import { SplitterModule } from 'primeng/splitter';
import { KeycloakService } from '../services/keycloak.service';

@Component({
  selector: 'app-dashboard',
  imports: [SplitterModule, PanelMenu, RouterOutlet],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss',
  host: {
    class: 'dashboard-shell',
  },
})
export class Dashboard {
  private readonly keycloak = inject(KeycloakService);

  readonly items = signal<MenuItem[]>([
      {
        label: 'Events List',
        routerLink: 'events'
      },
      {
        label: 'My Bookings',
        routerLink: 'bookings'
      },
      {
        label: 'Logout',
        icon: 'pi pi-sign-out',
        command: () => {
          this.logout();
        }
      }
    ]);

  logout(): void {
    this.keycloak.logout();
  }
}
