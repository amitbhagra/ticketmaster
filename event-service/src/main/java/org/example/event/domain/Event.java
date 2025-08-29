package org.example.event.domain;

import jakarta.persistence.Entity;

import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Event extends BaseEntity {

	private String name;
	private EventCategory category;
	private String artist;
	@ManyToOne
	@JoinColumn(name = "venue_id")
	private Venue venue;
	private Timestamp eventDateTime;
	private BigDecimal ticketPrice;
	private Integer totalSeats;

}
