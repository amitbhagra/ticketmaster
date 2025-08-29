package org.example.eventslist.controller;

import org.example.eventslist.model.elasticsearch.Event;
import org.example.eventslist.service.EventGeoDistanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/eventslist")
public class EventGeoDistanceController {

    @Autowired
    private EventGeoDistanceService eventGeoDistanceService;

    @PostMapping("/distance")
    public ResponseEntity<List<Event>> findEventsSortedByDistance(@RequestBody Map<String, Double> location) throws Exception {
        double lat = location.getOrDefault("lat", 0.0);
        double lon = location.getOrDefault("lon", 0.0);
        List<Event> events = eventGeoDistanceService.findEventsSortedByDistance(lat, lon);
        return ResponseEntity.ok(events);
    }
}

