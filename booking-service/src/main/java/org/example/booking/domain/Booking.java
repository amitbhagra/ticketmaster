package org.example.booking.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity(name="booking")
public class Booking extends BaseEntity {

	@Enumerated
	private BookingStatus status;
	@Column(columnDefinition = "uuid")
	private UUID userId;
	private Long eventId;
	private Double amount;
	private int numberOfTickets;

}
