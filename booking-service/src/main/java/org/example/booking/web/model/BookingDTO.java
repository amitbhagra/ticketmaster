package org.example.booking.web.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.example.booking.domain.PaymentMethod;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class BookingDTO {
	private Long id;
	private Long eventId;
	private UUID userId;
	private Double amount;
	private int numberOfTickets;
}
