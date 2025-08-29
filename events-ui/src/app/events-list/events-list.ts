import { ScrollingModule } from '@angular/cdk/scrolling';
import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { ChangeDetectorRef, Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatListModule } from '@angular/material/list';
import { ButtonModule } from 'primeng/button';
import { Card } from 'primeng/card';
import { ChipModule } from 'primeng/chip';
import { DataView } from 'primeng/dataview';
import { DatePicker, DatePickerModule } from 'primeng/datepicker';
import { Dialog } from 'primeng/dialog';
import { InputNumber } from 'primeng/inputnumber';
import { InputTextModule } from 'primeng/inputtext';
import { SelectModule } from 'primeng/select';
import { TagModule } from 'primeng/tag';
import { Observable, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { ApiConfigService } from '../services/api-config.service';
import { KeycloakService } from '../services/keycloak.service';
import { InputOtpModule } from 'primeng/inputotp';
import { Router } from '@angular/router';
import { ToastModule } from 'primeng/toast';
import { MessageService } from 'primeng/api';
import { TableModule } from 'primeng/table';
import { timer } from 'rxjs';
import { DEFAULT_USER_ID } from '../constants/user.constants';

interface Event {
  name: string;
  description: string;
  venue: string;
  date: string;
  time: string;
  price: number;
}

@Component({
  selector: 'app-events-list',
  standalone: true,
  imports: [CommonModule, MatListModule, ScrollingModule, TagModule, ButtonModule, Dialog, InputNumber,
    InputTextModule, FormsModule, SelectModule, ChipModule, Card, DatePickerModule, InputOtpModule, ToastModule
    , TableModule],
  templateUrl: './events-list.html',
  styleUrls: ['./events-list.scss'],
  providers: [MessageService]
})
export class EventsList implements OnInit {

  // events: Event[] = [];
  errorMessage: string | null = null;
  visible: boolean = false;
  showCreateNewEventModal: boolean = false;
  selectedEventForBooking: any = {};
  booking: Booking = { quantity: 1, totalPrice: 0, paymentMode: '' };
  private http = inject(HttpClient);
  private keycloak = inject(KeycloakService);
  private cdr = inject(ChangeDetectorRef);
  private apiConfig = inject(ApiConfigService);
  
  newEvent: NewEvent = {};
  paymentModeList = [
    { name: 'Credit Card', code: 'CREDIT_CARD' },
    { name: 'Debit Card', code: 'DEBIT_CARD' },
    { name: 'Net Banking', code: 'NET_BANKING' },
    { name: 'Mobile Wallets', code: 'WALLET' },
    { name: 'UPI', code: 'UPI' },
    { name: 'Cash on Delivery', code: 'CASH_ON_DELIVERY' },
  ];
  showPaymentDialog: boolean = false;
  paymentLoading: boolean = false;;
  validatePaymentPayload: any = { id: '', otp: "", paymentMethod: "" };
  venueOptions: string[] = [];
  lat: number = 0;
  lon: number = 0;

  constructor(public router: Router, private messageService: MessageService) {

  }

  ngOnInit(): void {
    this.getLocationAndFetchEvents();
    console.log('fetching venues');
    this.fetchVenueOptions();
  }

  getLocationAndFetchEvents() {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          this.lat = position.coords.latitude;
          this.lon = position.coords.longitude;
          this.fetchEvents().subscribe({
            next: data => {
              this.events.set(data);
              this.errorMessage = null;
              this.cdr.detectChanges();
            },
            error: () => {
              this.events.set([]);
              this.errorMessage = 'Failed to load events. Please try again later.';
              this.cdr.detectChanges();
            }
          });
        },
        () => {
          // fallback to default location if permission denied
          this.lat = 22.728392;
          this.lon = 71.637077;
          this.fetchEvents().subscribe({
            next: data => {
              this.events.set(data);
              this.errorMessage = null;
              this.cdr.detectChanges();
            },
            error: () => {
              this.events.set([]);
              this.errorMessage = 'Failed to load events. Please try again later.';
              this.cdr.detectChanges();
            }
          });
        }
      );
    } else {
      // fallback to default location if geolocation not supported
      this.lat = 22.728392;
      this.lon = 71.637077;
      this.fetchEvents().subscribe({
        next: data => {
          this.events.set(data);
          this.errorMessage = null;
          this.cdr.detectChanges();
        },
        error: () => {
          this.events.set([]);
          this.errorMessage = 'Failed to load events. Please try again later.';
          this.cdr.detectChanges();
        }
      });
    }
  }

  fetchEvents(): Observable<Event[]> {
    const headers = new HttpHeaders().set('Content-Type', 'application/json').set('ngrok-skip-browser-warning', 'true');
    const baseUrl = this.apiConfig.getBaseUrl();
    return this.http.post<Event[]>(`${baseUrl}/api/v1/eventslist/distance`, {
      lat: this.lat,
      lon: this.lon
    }, { headers }).pipe(
      catchError(() => {
        this.errorMessage = 'Failed to load events. Please try again later.';
        return of([]);
      })
    );
  }

  fetchVenueOptions() {
    const headers = new HttpHeaders().set('Content-Type', 'application/json').set('ngrok-skip-browser-warning', 'true');
    const baseUrl = this.apiConfig.getBaseUrl();
    this.http.get<string[]>(`${baseUrl}/api/v1/events/venues`, { headers }).subscribe({
      next: (data) => {
        this.venueOptions = data;
        this.cdr.detectChanges();
      },
      error: () => {
        this.venueOptions = [];
      }
    });
  }

  events = signal<any>([]);

  onQuantityChange(newValue: number) {
    this.booking.totalPrice = newValue * this.selectedEventForBooking.price;
  }

  onDialogClose() {
    this.visible = false;
    // Always reset booking to a defined object
    this.booking = { quantity: 1, totalPrice: 0, paymentMode: '' };
  }

  createNewEvent() {
    const baseUrl = this.apiConfig.getBaseUrl();
    const headers = { 'Content-Type': 'application/json' };
    this.http.post(`${baseUrl}/api/v1/events`, this.newEvent, { headers }).subscribe({
      next: (response) => {
        // Optionally refresh events or show a success message
        this.fetchEvents().subscribe({
          next: data => {
            this.events.set(data);
            this.cdr.detectChanges();
          }
        });
        this.showCreateNewEventModal = false;
        // clear newEvent form
        this.newEvent = {};
        this.cdr.detectChanges();

      },
      error: () => {
        this.errorMessage = 'Failed to create event. Please try again.';
        this.cdr.detectChanges();
      }
    });
  }

  createNewBooking() {
    this.validatePaymentPayload = { id: '', otp: "", paymentMethod: "" };
    const baseUrl = this.apiConfig.getBaseUrl();
    const bookingPayload = {
      eventId: this.selectedEventForBooking.id,
      userId: this.keycloak.getSubject() ? this.keycloak.getSubject() : DEFAULT_USER_ID,
      numberOfTickets: this.booking.quantity,
      amount: (this.selectedEventForBooking.ticketPrice || 0) * (this.booking.quantity || 0)
    };
    const headers = { 'Content-Type': 'application/json' };
    this.http.post(`${baseUrl}/api/v1/bookings`, bookingPayload, { headers }).subscribe({
      next: (response: any) => {
        // Optionally handle success, close modal, refresh bookings/events, etc.
        this.visible = false;
        // Always reset booking to a defined object
        this.booking = { quantity: 1, totalPrice: 0, paymentMode: '' };
        this.showPaymentDialog = true;
        this.initiatePayment(response.id);
        // this.cdr.detectChanges();
        // this.ref = this.dialogService.open(ProductListDemo, { header: 'Select a Product'});
      },
      error: () => {
        this.errorMessage = 'Failed to create booking. Please try again.';
        // this.cdr.detectChanges();
      }
    });
  }

  initiatePayment(id: number): void {
    this.paymentLoading = true;

    const url = `${this.apiConfig.getBaseUrl()}/api/v1/payments/initiate/${id}`;
    // Add a 3-second delay before making the HTTP request
    timer(1500).subscribe(() => {
      this.http.get<any[]>(url).subscribe({
        next: (data: any) => {
          this.paymentLoading = false;
          this.validatePaymentPayload = { otp: data.otp, id: data.id };
          this.cdr.detectChanges();
        },
        error: (err) => {
          this.paymentLoading = false;
          this.cdr.detectChanges();
        }
      });
    });
  }


  validatePayment() {
    const baseUrl = this.apiConfig.getBaseUrl();
    this.http.post(`${baseUrl}/api/v1/payments/validate`, this.validatePaymentPayload).subscribe({
      next: (response: any) => {
        if (response.paymentStatus && response.paymentStatus == "APPROVED") {
          this.messageService.add({ severity: 'success', summary: 'Info', detail: 'Successfully created booking', life: 3000 });
          setTimeout(() => {
            this.router.navigate(['dashboard/bookings']);
          }, 2000);

        } else if (response.paymentStatus && response.paymentStatus == "DECLINED") {
          this.messageService.add({ severity: 'danger', summary: 'Error', detail: 'Failed to create booking. Please try again.', life: 3000 });
        }
      },
      error: (error) => {
        console.error('POST request failed:', error);

        this.cdr.detectChanges();
      },
      complete: () => {
        console.log('POST request completed.');
        // Optional: Actions to perform when the observable completes
      }
    });


  }

  getUsername(): string {
    const username = this.keycloak.getUsername ? this.keycloak.getUsername() : '';
    return username ?? '';
  }

  isAdminUser(): boolean {
    if (!this.keycloak.getSubject()) return true;
    const isAdmin = this.keycloak.isAdmin() ? this.keycloak.isAdmin() : false;
    return isAdmin ?? false;
  }
}

export interface Booking {
  id?: string;
  code?: string;
  totalPrice?: number;
  quantity?: number;
  paymentMode?: string;
}

export interface NewEvent {
  id?: string;
  artist?: string;
  name?: string;
  category?: string;
  date?: DatePicker;
  venue?: number;
  time?: string;
  ticketPrice?: number;
  totalSeats?: number;
}