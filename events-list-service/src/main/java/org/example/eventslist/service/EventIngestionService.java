package org.example.eventslist.service;

import org.example.eventslist.model.elasticsearch.Event;
import org.example.eventslist.model.kafka.OutboxEvent;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.core.IndexRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class EventIngestionService {
    @Autowired
    private OpenSearchClient openSearchClient;

    public String ingestEvent(Event event) throws Exception {
        IndexRequest<Event> request = IndexRequest.of(i -> i
            .index("events")
            .document(event)
        );
        var response = openSearchClient.index(request);
        return response.id();
    }

    public void processEvent(OutboxEvent message) throws Exception {
        Event event = new Event();
        event.setId(String.valueOf(message.getId()));
        event.setName(message.getName());
        event.setVenue(message.getVenue().getName());
        event.setLocation(new GeoPoint(message.getVenue().getLat(), message.getVenue().getLon()));
        event.setArtist(message.getArtist());
        event.setCategory(message.getCategory());
        event.setArtist(message.getArtist());
        event.setTicketPrice(BigDecimal.valueOf(message.getTicketPrice()));
        event.setTotalSeats(message.getTotalSeats());
        if (message.getEventDateTime() != null) {
            event.setEventDateTime(LocalDateTime.from(message.getEventDateTime()));
        }

        ingestEvent(event);

    }
}

