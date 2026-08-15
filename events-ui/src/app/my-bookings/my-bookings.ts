import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component, computed, inject, resource, signal } from '@angular/core';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { DialogModule } from 'primeng/dialog';
import { TableModule } from 'primeng/table';
import { firstValueFrom } from 'rxjs';
import { DEFAULT_USER_ID } from '../constants/user.constants';
import { ApiConfigService } from '../services/api-config.service';
import { KeycloakService } from '../services/keycloak.service';

interface BookingListItem {
  amount: number;
  bookingDateAndTime: string;
  bookingStatus: string;
  id: number;
  numberOfTickets: number;
}

interface BookingInfo {
  amount: number;
  bookingDateAndTime: string;
  bookingStatus: string;
  numberOfTickets: number;
}

interface PaymentInfo {
  amount: number;
  bookingId: number;
  paymentMethod: string;
  status: string;
}

interface EventInfo {
  artist?: string;
  date: string;
  description?: string;
  name: string;
  time: string;
  venue: string;
}

interface BookingDetails {
  bookingInfo: BookingInfo;
  eventInfo: EventInfo;
  paymentInfo: PaymentInfo;
}

@Component({
  selector: 'app-my-bookings',
  imports: [CommonModule, TableModule, ButtonModule, CardModule, DialogModule],
  templateUrl: './my-bookings.html',
  styleUrl: './my-bookings.scss',
  host: {
    class: 'bookings-page',
  },
})
export class MyBookings {
  private readonly http = inject(HttpClient);
  private readonly apiConfig = inject(ApiConfigService);
  private readonly keycloak = inject(KeycloakService);

  private readonly refreshTick = signal(0);
  readonly selectedBookingId = signal<number | null>(null);
  readonly showDialog = signal(false);

  readonly username = computed<string>(() => this.keycloak.getUsername() ?? 'Guest User');
  readonly userId = computed<string>(() => this.keycloak.getSubject() ?? DEFAULT_USER_ID);

  readonly bookingsResource = resource<BookingListItem[], { refresh: number; userId: string }>({
    defaultValue: [],
    params: () => ({
      refresh: this.refreshTick(),
      userId: this.userId(),
    }),
    loader: async ({ params }) => {
      const url = `${this.apiConfig.getBaseUrl()}/api/v1/bookings/user/${params.userId}`;
      return firstValueFrom(this.http.get<BookingListItem[]>(url));
    },
  });

  readonly bookingDetailsResource = resource<BookingDetails | null, { bookingId: number | null }>({
    defaultValue: null,
    params: () => ({ bookingId: this.selectedBookingId() }),
    loader: async ({ params }) => {
      if (params.bookingId === null) {
        return null;
      }

      const url = `${this.apiConfig.getBaseUrl()}/api/v1/bookings/${params.bookingId}`;
      return firstValueFrom(this.http.get<BookingDetails>(url));
    },
  });

  readonly bookings = computed<BookingListItem[]>(() => this.bookingsResource.value());
  readonly loading = computed<boolean>(() => {
    const status = this.bookingsResource.status();
    return status === 'loading' || status === 'reloading';
  });

  readonly selectedBooking = computed<BookingDetails | null>(() => this.bookingDetailsResource.value());
  readonly selectedBookingLoading = computed<boolean>(() => {
    const status = this.bookingDetailsResource.status();
    return this.showDialog() && (status === 'loading' || status === 'reloading');
  });

  onRowClick(booking: BookingListItem): void {
    this.selectedBookingId.set(booking.id);
    this.showDialog.set(true);
  }

  closeDialog(): void {
    this.showDialog.set(false);
    this.selectedBookingId.set(null);
  }
}
