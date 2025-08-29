package org.example.booking.web.controllers;
import org.example.booking.web.model.BookingDTO;
import org.example.booking.service.BookingService;
import org.example.booking.web.model.BookingStatusDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/v1/bookings")
public class BookingsController {

	private final BookingService bookingService;

	public BookingsController (BookingService bookingService) {
		super();
		this.bookingService = bookingService;
	}

	@PostMapping("")
	public BookingDTO createBooking(@RequestBody BookingDTO dto) {
		return this.bookingService.create(dto);
	}

	@GetMapping("/{id}")
	public ResponseEntity<BookingStatusDTO> getBookingById(@PathVariable("id") long id) {
		BookingStatusDTO bookingStatusDTO = bookingService.getBookingById(id);

		if (bookingStatusDTO != null) {
			return new ResponseEntity<>(bookingStatusDTO, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@GetMapping("/user/{userId}")
	public ResponseEntity<List<BookingStatusDTO>> getBookingsByUserId(@PathVariable("userId") UUID userId) {
		List<BookingStatusDTO> bookingStatusDTOList = bookingService.getBookingsByUserId(userId);

		if (bookingStatusDTOList != null) {
			return new ResponseEntity<>(bookingStatusDTOList, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}
}
