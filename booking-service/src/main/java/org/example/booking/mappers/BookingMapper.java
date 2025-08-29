package org.example.booking.mappers;

import org.example.booking.domain.Booking;
import org.example.booking.web.model.BookingDTO;
import org.example.booking.web.model.BookingStatusDTO;
import org.mapstruct.Mapper;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface BookingMapper {
    BookingDTO bookingToBookingDto(Booking entity);

    default BookingStatusDTO bookingToBookingStatusDto(Booking entity) {
        if (entity == null) return null;
        BookingStatusDTO dto = new BookingStatusDTO();
        dto.setBookingStatus(entity.getStatus());
        if (entity.getCreateTs() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm");
            dto.setBookingDateAndTime(entity.getCreateTs().toLocalDateTime().format(formatter));
        }
        // Set other fields as needed
        dto.setAmount(entity.getAmount());
        dto.setNumberOfTickets(entity.getNumberOfTickets());
        dto.setEventId(entity.getEventId());
        dto.setUserId(entity.getUserId());
        dto.setId(entity.getId());
        return dto;
    }

    default List<BookingStatusDTO> bookingListToBookingStatusList(List<Booking> bookings) {
        if (bookings == null) return null;
        return bookings.stream()
                .map(this::bookingToBookingStatusDto)
                .collect(Collectors.toList());
    }

    Booking bookingDtoToBooking(BookingDTO dto);
}
