package org.example.booking.service;

import org.example.booking.domain.Booking;
import org.example.booking.domain.PaymentStatus;

public interface BookingSagaManager {

    Booking newBooking(Booking booking);

    public void processPaymentResult(Long bookingId, PaymentStatus status);


}
