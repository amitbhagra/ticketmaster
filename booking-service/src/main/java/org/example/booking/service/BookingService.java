package org.example.booking.service;

import org.example.booking.web.model.BookingDTO;
import org.example.booking.web.model.BookingStatusDTO;

import java.util.List;
import java.util.UUID;

public interface BookingService {
    BookingDTO create(BookingDTO bookingDTO);

    BookingStatusDTO getBookingById(long id);

    List<BookingStatusDTO> getBookingsByUserId(UUID userId);
}
