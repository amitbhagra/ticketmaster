package org.example.eventslist.model.kafka;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Setter
@Getter
public class Venue {
    private Long id;
    private Integer version;
    private Instant createTs;
    private Instant updateTs;
    private String name;
    private Double lat;
    private Double lon;

    // getters and setters
}