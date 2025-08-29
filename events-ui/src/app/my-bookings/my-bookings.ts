import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { ChipModule } from 'primeng/chip';
import { HttpClient } from '@angular/common/http';
import { ApiConfigService } from '../services/api-config.service';
import { DialogModule } from 'primeng/dialog';
import { CommonModule } from '@angular/common';
import { KeycloakService } from '../services/keycloak.service';
import { DEFAULT_USER_ID } from '../constants/user.constants';

@Component({
  selector: 'app-my-bookings',
  imports: [CommonModule, TableModule, ButtonModule, CardModule, ChipModule, DialogModule],
  templateUrl: './my-bookings.html',
  styleUrl: './my-bookings.scss'
})
export class MyBookings implements OnInit {
  bookings: any = [];
  selectedBooking: any = null;
  showDialog: boolean = false;
  loading: boolean = false;
  private keycloak = inject(KeycloakService);

  constructor(private http: HttpClient, private apiConfig: ApiConfigService,private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.loading = true;
    const userId = this.keycloak.getSubject() ? this.keycloak.getSubject() : DEFAULT_USER_ID;
    const url = `${this.apiConfig.getBaseUrl()}/api/v1/bookings/user/` + userId;
    this.http.get<any[]>(url).subscribe({
      next: (data) => {
        this.bookings = data;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.bookings = [];
        this.loading = false;
      }
    });
  }

  onRowClick(booking: any) {
    const url = `${this.apiConfig.getBaseUrl()}/api/v1/bookings/${booking.id}`;
    this.http.get<any>(url).subscribe({
      next: (data) => {
        this.selectedBooking = data;
        this.showDialog = true;
        this.cdr.detectChanges();
      },
      error: () => {
        this.selectedBooking = null;
        this.showDialog = false;
      }
    });
  }

  closeDialog() {
    this.showDialog = false;
    this.selectedBooking = null;
  }

  getUsername(): string {
    const username = this.keycloak.getUsername ? this.keycloak.getUsername() : '';
    return username ?? '';
  }
}
