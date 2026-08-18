import { ScrollingModule } from '@angular/cdk/scrolling';
import { CommonModule, CurrencyPipe, DatePipe } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Component, computed, inject, linkedSignal, resource, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatListModule } from '@angular/material/list';
import { Router } from '@angular/router';
import { MessageService } from 'primeng/api';
import { ButtonModule } from 'primeng/button';
import { Card } from 'primeng/card';
import { ChipModule } from 'primeng/chip';
import { DatePickerModule } from 'primeng/datepicker';
import { Dialog } from 'primeng/dialog';
import { InputNumber } from 'primeng/inputnumber';
import { InputOtpModule } from 'primeng/inputotp';
import { InputTextModule } from 'primeng/inputtext';
import { SelectModule } from 'primeng/select';
import { TableModule } from 'primeng/table';
import { ToastModule } from 'primeng/toast';
import { firstValueFrom } from 'rxjs';
import { DEFAULT_USER_ID } from '../constants/user.constants';
import { ApiConfigService } from '../services/api-config.service';
import { KeycloakService } from '../services/keycloak.service';

const FALLBACK_LATITUDE = 22.728392;
const FALLBACK_LONGITUDE = 71.637077;

type PaymentModeCode =
  | 'CREDIT_CARD'
  | 'DEBIT_CARD'
  | 'NET_BANKING'
  | 'WALLET'
  | 'UPI'
  | 'CASH_ON_DELIVERY';

interface EventItem {
  id: number;
  artist: string;
  category: string;
  date: string;
  description?: string;
  name: string;
  ticketPrice: number;
  time: string;
  totalSeats: number;
  venue: string;
}

interface BookingDraft {
  eventId: number;
  paymentMode: PaymentModeCode | '';
  quantity: number;
}

interface NewEventDraft {
  artist: string;
  category: string;
  date: Date | null;
  name: string;
  ticketPrice: number | null;
  time: string;
  totalSeats: number | null;
  venue: string;
}

interface CreateBookingResponse {
  id: number;
}

interface InitiatePaymentResponse {
  id: number;
  otp: string;
}

interface ValidatePaymentPayload {
  id: number | null;
  otp: string;
  paymentMode: PaymentModeCode | '';
}

interface ValidatePaymentResponse {
  paymentStatus?: 'APPROVED' | 'DECLINED' | string;
}

interface PaymentModeOption {
  code: PaymentModeCode;
  name: string;
}

interface VenueOption {
  id: number;
  name: string;
}

@Component({
  selector: 'app-events-list',
  imports: [
    CommonModule,
    MatListModule,
    ScrollingModule,
    ButtonModule,
    Dialog,
    InputNumber,
    InputTextModule,
    FormsModule,
    SelectModule,
    ChipModule,
    Card,
    DatePickerModule,
    InputOtpModule,
    ToastModule,
    TableModule,
    DatePipe,
    CurrencyPipe,
  ],
  templateUrl: './events-list.html',
  styleUrl: './events-list.scss',
  providers: [MessageService],
  host: {
    class: 'events-page',
  },
})
export class EventsList {
  private readonly http = inject(HttpClient);
  private readonly keycloak = inject(KeycloakService);
  private readonly apiConfig = inject(ApiConfigService);
  private readonly router = inject(Router);
  private readonly messageService = inject(MessageService);

  readonly showBookingDialog = signal(false);
  readonly showCreateNewEventModal = signal(false);
  readonly showPaymentDialog = signal(false);
  readonly paymentLoading = signal(false);

  private readonly latitude = signal(FALLBACK_LATITUDE);
  private readonly longitude = signal(FALLBACK_LONGITUDE);
  private readonly eventsReload = signal(0);
  private readonly venueReload = signal(0);

  readonly eventsResource = resource<EventItem[], { lat: number; lon: number; refresh: number }>({
    defaultValue: [],
    params: () => ({
      lat: this.latitude(),
      lon: this.longitude(),
      refresh: this.eventsReload(),
    }),
    loader: async ({ params }) => {
      const headers = new HttpHeaders()
        .set('Content-Type', 'application/json')
        .set('ngrok-skip-browser-warning', 'true');

      return firstValueFrom(
        this.http.post<EventItem[]>(
          `${this.apiConfig.getBaseUrl()}/api/v1/eventslist/distance`,
          { lat: params.lat, lon: params.lon },
          { headers }
        )
      );
    },
  });

  readonly venueOptionsResource = resource<VenueOption[], number>({
    defaultValue: [],
    params: () => this.venueReload(),
    loader: async () => {
      const headers = new HttpHeaders()
        .set('Content-Type', 'application/json')
        .set('ngrok-skip-browser-warning', 'true');

      return firstValueFrom(this.http.get<VenueOption[]>(`${this.apiConfig.getBaseUrl()}/api/v1/events/venues`, { headers }));
    },
  });

  readonly paymentModeList: PaymentModeOption[] = [
    { name: 'Credit Card', code: 'CREDIT_CARD' },
    { name: 'Debit Card', code: 'DEBIT_CARD' },
    { name: 'Net Banking', code: 'NET_BANKING' },
    { name: 'Mobile Wallets', code: 'WALLET' },
    { name: 'UPI', code: 'UPI' },
    { name: 'Cash on Delivery', code: 'CASH_ON_DELIVERY' },
  ];

  readonly events = computed<EventItem[]>(() => this.eventsResource.value());
  readonly selectedEvent = signal<EventItem | null>(null);
  readonly eventsErrorMessage = computed<string | null>(() => {
    if (this.eventsResource.status() === 'error') {
      return 'Failed to load events. Please try again later.';
    }
    return null;
  });
  readonly venueOptions = computed<VenueOption[]>(() => this.venueOptionsResource.value());
  readonly username = computed<string>(() => this.keycloak.getUsername() ?? 'Guest User');
  readonly isAdminUser = computed<boolean>(() => {
    if (!this.keycloak.getSubject()) {
      return true;
    }
    return this.keycloak.isAdmin();
  });

  readonly booking = linkedSignal<BookingDraft>(() => {
    const currentEvent = this.selectedEvent();
    return {
      eventId: currentEvent?.id ?? 0,
      paymentMode: '',
      quantity: 1,
    };
  });

  readonly totalPrice = computed<number>(() => {
    const currentEvent = this.selectedEvent();
    if (!currentEvent) {
      return 0;
    }
    return currentEvent.ticketPrice * this.booking().quantity;
  });

  readonly newEvent = signal<NewEventDraft>({
    artist: '',
    category: '',
    date: null,
    name: '',
    ticketPrice: null,
    time: '',
    totalSeats: null,
    venue: '',
  });

  readonly validatePaymentPayload = signal<ValidatePaymentPayload>({
    id: null,
    otp: '',
    paymentMode: '',
  });

  constructor() {
    void this.resolveLocation();
  }

  private async resolveLocation(): Promise<void> {
    if (!navigator.geolocation) {
      return;
    }

    await new Promise<void>((resolve) => {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          this.latitude.set(position.coords.latitude);
          this.longitude.set(position.coords.longitude);
          resolve();
        },
        () => {
          resolve();
        },
      );
    });
  }

  openBookingDialog(eventItem: EventItem): void {
    this.selectedEvent.set(eventItem);
    this.showBookingDialog.set(true);
  }

  closeBookingDialog(): void {
    this.showBookingDialog.set(false);
    this.selectedEvent.set(null);
  }

  onQuantityChange(newQuantity: number | null): void {
    const seats = this.selectedEvent()?.totalSeats ?? 1;
    const quantity = Math.max(1, Math.min(newQuantity ?? 1, seats));
    this.booking.update((current) => ({ ...current, quantity }));
  }

  updateNewEventField<Key extends keyof NewEventDraft>(field: Key, value: NewEventDraft[Key]): void {
    this.newEvent.update((current) => ({ ...current, [field]: value }));
  }

  async createNewEvent(): Promise<void> {
    const draft = this.newEvent();
    if (!draft.name || !draft.artist || !draft.category || !draft.venue || !draft.date || !draft.ticketPrice || !draft.totalSeats) {
      this.messageService.add({ severity: 'warn', summary: 'Validation', detail: 'Please complete all required fields.', life: 2500 });
      return;
    }

    await firstValueFrom(
      this.http.post(
        `${this.apiConfig.getBaseUrl()}/api/v1/events`,
        {
          ...draft,
          date: draft.date.toISOString(),
        },
        { headers: { 'Content-Type': 'application/json' } }
      )
    );

    this.showCreateNewEventModal.set(false);
    this.newEvent.set({
      artist: '',
      category: '',
      date: null,
      name: '',
      ticketPrice: null,
      time: '',
      totalSeats: null,
      venue: '',
    });
    this.eventsReload.update((tick) => tick + 1);
    this.messageService.add({ severity: 'success', summary: 'Success', detail: 'Event created successfully.', life: 2500 });
  }

  async createNewBooking(): Promise<void> {
    const eventItem = this.selectedEvent();
    if (!eventItem) {
      return;
    }

    this.validatePaymentPayload.set({ id: null, otp: '', paymentMode: '' });

    const bookingPayload = {
      amount: eventItem.ticketPrice * this.booking().quantity,
      eventId: eventItem.id,
      numberOfTickets: this.booking().quantity,
      userId: this.keycloak.getSubject() ?? DEFAULT_USER_ID,
    };

    const response = await firstValueFrom(
      this.http.post<CreateBookingResponse>(`${this.apiConfig.getBaseUrl()}/api/v1/bookings`, bookingPayload, {
        headers: { 'Content-Type': 'application/json' },
      })
    );

    this.showBookingDialog.set(false);
    this.showPaymentDialog.set(true);
    await this.initiatePayment(response.id);
  }

  private async initiatePayment(bookingId: number): Promise<void> {
    this.paymentLoading.set(true);

    const url = `${this.apiConfig.getBaseUrl()}/api/v1/payments/initiate/${bookingId}`;
    await new Promise<void>((resolve) => {
      setTimeout(() => {
        resolve();
      }, 1500);
    });

    try {
      const data = await firstValueFrom(this.http.get<InitiatePaymentResponse>(url));
      this.validatePaymentPayload.update((payload) => ({
        ...payload,
        id: data.id,
        otp: data.otp,
      }));
    } finally {
      this.paymentLoading.set(false);
    }
  }

  updatePaymentOtp(otp: string): void {
    this.validatePaymentPayload.update((payload) => ({ ...payload, otp }));
  }

  updatePaymentMode(paymentMode: PaymentModeCode | ''): void {
    this.validatePaymentPayload.update((payload) => ({ ...payload, paymentMode }));
  }

  async validatePayment(): Promise<void> {
    const payload = this.validatePaymentPayload();
    if (!payload.id || !payload.otp || !payload.paymentMode) {
      return;
    }

    const response = await firstValueFrom(
      this.http.post<ValidatePaymentResponse>(`${this.apiConfig.getBaseUrl()}/api/v1/payments/validate`, payload)
    );

    if (response.paymentStatus === 'APPROVED') {
      this.messageService.add({
        severity: 'success',
        summary: 'Success',
        detail: 'Successfully created booking.',
        life: 2500,
      });
      this.showPaymentDialog.set(false);
      this.eventsReload.update((tick) => tick + 1);
      setTimeout(() => {
        void this.router.navigate(['dashboard/bookings']);
      }, 1000);
      return;
    }

    this.messageService.add({
      severity: 'error',
      summary: 'Payment Failed',
      detail: 'Failed to create booking. Please try again.',
      life: 3000,
    });
  }

  closePaymentDialog(): void {
    this.showPaymentDialog.set(false);
    this.validatePaymentPayload.set({ id: null, otp: '', paymentMode: '' });
  }
}