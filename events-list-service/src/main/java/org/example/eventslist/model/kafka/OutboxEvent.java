package org.example.eventslist.model.kafka;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Setter
@Getter
public class OutboxEvent {
    private Long id;
    private Integer version;
    private Instant createTs;
    private Instant updateTs;
    private String name;
    private String category;
    private String artist;
    private Venue venue;
    private Instant eventDateTime;
    private Integer ticketPrice;
    private Integer totalSeats;

    // getters and setters
}