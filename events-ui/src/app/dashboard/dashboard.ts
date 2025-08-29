import { CommonModule } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { MenuItem } from 'primeng/api';
import { PanelMenu } from 'primeng/panelmenu';
import { SplitterModule } from 'primeng/splitter';
import { KeycloakService } from '../services/keycloak.service';
@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule,  SplitterModule, PanelMenu],
  templateUrl: './dashboard.html',
  styleUrls: ['./dashboard.scss']
})
export class Dashboard implements OnInit {
  private keycloak = inject(KeycloakService);
  items: MenuItem[] = [];

  constructor(private router: Router) {}
  ngOnInit() {
    this.items = [
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
    ];
  }

  logout() {
    this.keycloak.logout();
  }
}
