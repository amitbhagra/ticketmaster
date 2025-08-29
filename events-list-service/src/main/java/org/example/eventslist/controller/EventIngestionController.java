package org.example.eventslist.controller;

import org.example.eventslist.model.elasticsearch.Event;
import org.example.eventslist.service.EventIngestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/events")
public class EventIngestionController {
    @Autowired
    private EventIngestionService eventIngestionService;

    @PostMapping("/ingest")
    public ResponseEntity<?> ingestEvent(@RequestBody Event event) {
        try {
            String id = eventIngestionService.ingestEvent(event);
            // Create a
            return ResponseEntity.ok().body("Event ingested with ID: " + id);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error ingesting event: " + e.getMessage());
        }
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception ex) {
        return ResponseEntity.status(500).body("Internal server error: " + ex.getMessage());
    }
}

