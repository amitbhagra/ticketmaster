package org.example.event.service.impl;

import java.util.List;
import java.util.Optional;

import org.example.event.domain.Venue;
import org.example.event.mapstruct.mappers.VenueMapper;
import org.example.event.repository.VenueRepository;
import org.example.event.web.model.VenueDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.example.event.domain.Event;
import org.example.event.mapstruct.mappers.EventMapper;
import org.example.event.repository.EventRepository;
import org.example.event.service.EventService;
import org.example.event.web.model.EventDto;
import org.example.event.domain.OutboxEvent;
import org.example.event.repository.OutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class EventServiceImpl implements EventService {

    @Autowired
    EventRepository eventRepository;
    @Autowired
    VenueRepository venueRepository;

    @Autowired
    EventMapper eventMapper;
    @Autowired
    VenueMapper venueMapper;
    @Autowired
    OutboxEventRepository outboxEventRepository;
    @Autowired
    ObjectMapper objectMapper;

    @Override
    public List<EventDto> getAllEvents() {
        List<Event> events = eventRepository.findAll();
        return eventMapper.eventListToEventDtoList(events);
    }

    @Override
    public List<VenueDto> getAllVenues() {
        List<Venue> venues = venueRepository.findAll();
        return venueMapper.venueListToVenueDtoList(venues);
    }

    @Override
    public EventDto getEventById(long id) {
        Optional<Event> event = eventRepository.findById(id);
        return event.map(value -> eventMapper.eventToEventDto(value)).orElse(null);
    }

    @Override
    @Transactional
    public EventDto create(EventDto eventDto) {

        // Fetch existing Venue
        Venue venue = venueRepository.findById(Long.valueOf(eventDto.getVenue())).orElseThrow();

        // Map EventDto to Event
        Event event = eventMapper.eventDtoToEvent(eventDto);

        // Set the managed Venue
        event.setVenue(venue);

        // Save Event
        event = eventRepository.save(event);

        try {
            String payload = objectMapper.writeValueAsString(event);
            OutboxEvent outboxEvent = new OutboxEvent();
            outboxEvent.setAggregateType("Event");
            outboxEvent.setAggregateId(event.getId().toString());
            outboxEvent.setType("EventCreated");
            outboxEvent.setPayload(payload);
            outboxEventRepository.save(outboxEvent);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to serialize event for outbox", ex);
        }

        return eventMapper.eventToEventDto(event);
    }

    @Override
    @Transactional
    public EventDto update(long id, EventDto eventDto) {
        EventDto resp = null;
        Optional<Event> eventOp = eventRepository.findById(id);

        if (eventOp.isPresent()) {
            Event event = eventOp.get();
            event.setName(eventDto.getName());
            resp = eventMapper.eventToEventDto(event);
        }
        return resp;

    }

    @Override
    public void delete(long id) {
        eventRepository.deleteById(id);
    }


}
