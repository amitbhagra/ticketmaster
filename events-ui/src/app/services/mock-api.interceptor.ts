import {
  HttpErrorResponse,
  HttpEvent,
  HttpHandlerFn,
  HttpInterceptorFn,
  HttpRequest,
  HttpResponse,
} from '@angular/common/http';
import { Observable, of, throwError } from 'rxjs';
import { delay } from 'rxjs/operators';

const MOCK_DELAY_MS = 220;

interface MockEvent {
  artist: string;
  category: string;
  date: string;
  description?: string;
  id: number;
  name: string;
  ticketPrice: number;
  time: string;
  totalSeats: number;
  venue: string;
}

interface MockBooking {
  amount: number;
  bookingDateAndTime: string;
  bookingStatus: 'PENDING_PAYMENT' | 'CONFIRMED' | 'PAYMENT_DECLINED';
  eventId: number;
  id: number;
  numberOfTickets: number;
  userId: string;
}

interface MockPayment {
  amount: number;
  bookingId: number;
  otp: string;
  paymentMethod: string;
  status: 'PENDING' | 'APPROVED' | 'DECLINED';
}

interface CreateEventPayload {
  artist?: string;
  category?: string;
  date?: string;
  name?: string;
  ticketPrice?: number;
  time?: string;
  totalSeats?: number;
  venue?: string;
}

interface CreateBookingPayload {
  amount?: number;
  eventId?: number;
  numberOfTickets?: number;
  userId?: string;
}

interface ValidatePaymentPayload {
  id?: number;
  otp?: string;
  paymentMode?: string;
}

const mockDb = createMockDb();

export const mockApiInterceptor: HttpInterceptorFn = (req, next) => {
  const path = extractPath(req.url);

  if (!path.startsWith('/api/v1/')) {
    return next(req);
  }

  if (req.method === 'POST' && path === '/api/v1/eventslist/distance') {
    return ok(mockDb.events.map((eventItem) => ({ ...eventItem })));
  }

  if (req.method === 'GET' && path === '/api/v1/events/venues') {
    return ok([...mockDb.venues]);
  }

  if (req.method === 'POST' && path === '/api/v1/events') {
    const payload = req.body as CreateEventPayload;
    if (!payload?.name || !payload.artist || !payload.category || !payload.venue || !payload.date) {
      return badRequest('Missing required event fields.');
    }

    const eventItem: MockEvent = {
      artist: payload.artist,
      category: payload.category,
      date: payload.date,
      id: mockDb.nextEventId++,
      name: payload.name,
      ticketPrice: Math.max(1, Number(payload.ticketPrice ?? 0)),
      time: payload.time ?? '19:00',
      totalSeats: Math.max(1, Number(payload.totalSeats ?? 1)),
      venue: payload.venue,
    };

    mockDb.events = [eventItem, ...mockDb.events];
    if (!mockDb.venues.includes(eventItem.venue)) {
      mockDb.venues.push(eventItem.venue);
    }

    return ok(eventItem, 201);
  }

  if (req.method === 'POST' && path === '/api/v1/bookings') {
    const payload = req.body as CreateBookingPayload;
    const eventItem = mockDb.events.find((item) => item.id === payload?.eventId);
    if (!eventItem || !payload?.userId || !payload.numberOfTickets || payload.numberOfTickets < 1) {
      return badRequest('Invalid booking request payload.');
    }

    const booking: MockBooking = {
      amount: Number(payload.amount ?? eventItem.ticketPrice * payload.numberOfTickets),
      bookingDateAndTime: new Date().toISOString(),
      bookingStatus: 'PENDING_PAYMENT',
      eventId: eventItem.id,
      id: mockDb.nextBookingId++,
      numberOfTickets: payload.numberOfTickets,
      userId: payload.userId,
    };

    mockDb.bookings.unshift(booking);

    const payment: MockPayment = {
      amount: booking.amount,
      bookingId: booking.id,
      otp: '',
      paymentMethod: '',
      status: 'PENDING',
    };
    mockDb.payments.set(booking.id, payment);

    return ok({ id: booking.id }, 201);
  }

  const bookingsByUserMatch = path.match(/^\/api\/v1\/bookings\/user\/(.+)$/);
  if (req.method === 'GET' && bookingsByUserMatch) {
    const userId = decodeURIComponent(bookingsByUserMatch[1]);
    const userBookings = mockDb.bookings
      .filter((booking) => booking.userId === userId)
      .map((booking) => ({
        amount: booking.amount,
        bookingDateAndTime: booking.bookingDateAndTime,
        bookingStatus: booking.bookingStatus,
        id: booking.id,
        numberOfTickets: booking.numberOfTickets,
      }));

    return ok(userBookings);
  }

  const bookingByIdMatch = path.match(/^\/api\/v1\/bookings\/(\d+)$/);
  if (req.method === 'GET' && bookingByIdMatch) {
    const bookingId = Number(bookingByIdMatch[1]);
    const booking = mockDb.bookings.find((item) => item.id === bookingId);
    if (!booking) {
      return notFound('Booking not found.');
    }

    const eventItem = mockDb.events.find((item) => item.id === booking.eventId);
    const payment = mockDb.payments.get(booking.id);

    return ok({
      bookingInfo: {
        amount: booking.amount,
        bookingDateAndTime: booking.bookingDateAndTime,
        bookingStatus: booking.bookingStatus,
        numberOfTickets: booking.numberOfTickets,
      },
      eventInfo: {
        artist: eventItem?.artist,
        date: eventItem?.date ?? '',
        description: eventItem?.description,
        name: eventItem?.name ?? 'Unknown Event',
        time: eventItem?.time ?? '',
        venue: eventItem?.venue ?? '',
      },
      paymentInfo: {
        amount: payment?.amount ?? booking.amount,
        bookingId: booking.id,
        paymentMethod: payment?.paymentMethod ?? '',
        status: payment?.status ?? 'PENDING',
      },
    });
  }

  const initiatePaymentMatch = path.match(/^\/api\/v1\/payments\/initiate\/(\d+)$/);
  if (req.method === 'GET' && initiatePaymentMatch) {
    const bookingId = Number(initiatePaymentMatch[1]);
    const booking = mockDb.bookings.find((item) => item.id === bookingId);
    if (!booking) {
      return notFound('Booking not found for payment.');
    }

    const payment = mockDb.payments.get(bookingId);
    if (!payment) {
      return notFound('Payment record not found.');
    }

    payment.otp = createOtp();
    payment.status = 'PENDING';

    return ok({ id: bookingId, otp: payment.otp });
  }

  if (req.method === 'POST' && path === '/api/v1/payments/validate') {
    const payload = req.body as ValidatePaymentPayload;
    if (!payload?.id || !payload.otp || !payload.paymentMode) {
      return badRequest('Invalid payment validation payload.');
    }

    const payment = mockDb.payments.get(payload.id);
    const booking = mockDb.bookings.find((item) => item.id === payload.id);

    if (!payment || !booking) {
      return notFound('Payment or booking not found.');
    }

    const approved = payment.otp === payload.otp;
    payment.paymentMethod = payload.paymentMode;
    payment.status = approved ? 'APPROVED' : 'DECLINED';
    booking.bookingStatus = approved ? 'CONFIRMED' : 'PAYMENT_DECLINED';

    return ok({ paymentStatus: approved ? 'APPROVED' : 'DECLINED' });
  }

  return next(req);
};

function createMockDb(): {
  bookings: MockBooking[];
  events: MockEvent[];
  nextBookingId: number;
  nextEventId: number;
  payments: Map<number, MockPayment>;
  venues: string[];
} {
  const events: MockEvent[] = [
    {
      artist: 'Arijit Singh',
      category: 'Music',
      date: dateAfterDays(4),
      description: 'Live concert with orchestra.',
      id: 101,
      name: 'Moonlight Echoes',
      ticketPrice: 1800,
      time: '19:30',
      totalSeats: 320,
      venue: 'Ahmedabad Arena',
    },
    {
      artist: 'Anubhav Singh Bassi',
      category: 'Comedy',
      date: dateAfterDays(7),
      description: 'Stand-up special.',
      id: 102,
      name: 'LOL Circuit',
      ticketPrice: 950,
      time: '20:00',
      totalSeats: 180,
      venue: 'Riverfront Hall',
    },
    {
      artist: 'A. R. Rahman Ensemble',
      category: 'Music',
      date: dateAfterDays(10),
      description: 'Film score and fusion night.',
      id: 103,
      name: 'Symphonic Pulse',
      ticketPrice: 2500,
      time: '18:45',
      totalSeats: 420,
      venue: 'Sardar Convention Center',
    },
    {
      artist: 'Theatre Collective',
      category: 'Drama',
      date: dateAfterDays(12),
      description: 'Contemporary stage production.',
      id: 104,
      name: 'Curtain Call',
      ticketPrice: 700,
      time: '17:00',
      totalSeats: 140,
      venue: 'City Stage',
    },
  ];

  return {
    bookings: [],
    events,
    nextBookingId: 2001,
    nextEventId: 105,
    payments: new Map<number, MockPayment>(),
    venues: [...new Set(events.map((eventItem) => eventItem.venue))],
  };
}

function dateAfterDays(days: number): string {
  const date = new Date();
  date.setDate(date.getDate() + days);
  return date.toISOString();
}

function createOtp(): string {
  return `${Math.floor(100000 + Math.random() * 900000)}`;
}

function extractPath(rawUrl: string): string {
  try {
    return new URL(rawUrl, 'http://localhost').pathname;
  } catch {
    return rawUrl;
  }
}

function ok<T>(body: T, status = 200): Observable<HttpEvent<T>> {
  return of(new HttpResponse<T>({ body, status })).pipe(delay(MOCK_DELAY_MS));
}

function badRequest(message: string): Observable<never> {
  return fail(400, message);
}

function notFound(message: string): Observable<never> {
  return fail(404, message);
}

function fail(status: number, message: string): Observable<never> {
  return throwError(() =>
    new HttpErrorResponse({
      error: { message },
      status,
      statusText: message,
    }),
  );
}
