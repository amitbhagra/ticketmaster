package org.example.event.web.model;

import jakarta.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class EventDto {
	private Long id;
	@NotBlank(message = "Event Name must not be blank")
	private String name;
	private String category;
	private String artist;
	@NotBlank(message = "Venue must not be blank")
	private String venue;
	@NotBlank(message = "Date must not be blank")
	private String date;
	@NotBlank(message = "Time must not be blank")
	private String time;
	private BigDecimal ticketPrice;
	private Integer totalSeats;
}
