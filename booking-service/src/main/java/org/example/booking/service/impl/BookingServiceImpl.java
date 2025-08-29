package org.example.booking.service.impl;

import org.example.booking.domain.Booking;
import org.example.booking.mappers.BookingMapper;
import org.example.booking.repository.BookingRepository;
import org.example.booking.web.model.BookingDTO;
import org.example.booking.service.BookingService;
import org.example.booking.web.model.BookingStatusDTO;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

	private final BookingMapper bookingMapper;
	private final BookingSagaManagerImpl bookingOperationOrchestrator;
	private final BookingRepository bookingRepository;

	@Override
	public BookingDTO create(BookingDTO bookingDTO) {
		Booking booking = bookingMapper.bookingDtoToBooking(bookingDTO);

		String currentUserId = getCurrentUserId();
		if (currentUserId != null && !currentUserId.isEmpty()) {
			booking.setUserId(UUID.fromString(currentUserId));
		}

		Booking savedBooking = bookingOperationOrchestrator.newBooking(booking);
		log.debug("Saved Booking: {}", savedBooking);
		return bookingMapper.bookingToBookingDto(savedBooking);
	}

	@Override
	public BookingStatusDTO getBookingById(long id) {
		Optional<Booking> booking = bookingRepository.findById(id);
		return booking.map(bookingMapper::bookingToBookingStatusDto).orElse(null);
	}

	@Override
	public List<BookingStatusDTO> getBookingsByUserId(UUID userId) {
		String currentUserId = getCurrentUserId();
		if (currentUserId != null && !currentUserId.isEmpty()) {
			userId = UUID.fromString(currentUserId);
		}
		List<Booking> bookingList = bookingRepository.findByUserIdOrderByCreateTsDesc(userId);
		return bookingMapper.bookingListToBookingStatusList(bookingList);
	}

	public String getCurrentUserId() {
		if (SecurityContextHolder.getContext().getAuthentication() != null && !SecurityContextHolder.getContext().getAuthentication().getName().equalsIgnoreCase("anonymousUser")) {
			return SecurityContextHolder.getContext().getAuthentication().getName();
		}
		return null;
	}
}
