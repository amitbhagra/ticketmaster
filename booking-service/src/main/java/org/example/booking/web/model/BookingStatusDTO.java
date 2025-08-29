package org.example.booking.web.model;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.example.booking.domain.BookingStatus;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class BookingStatusDTO extends BookingDTO {

    private Long id;
    private BookingStatus bookingStatus;
    private String bookingDateAndTime;

}
