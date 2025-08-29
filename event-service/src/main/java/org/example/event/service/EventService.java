package org.example.event.service;

import java.util.List;

import org.example.event.web.model.EventDto;
import org.example.event.web.model.VenueDto;

public interface EventService {
	List<EventDto> getAllEvents();
	List<VenueDto> getAllVenues();
	EventDto getEventById(long id);
	EventDto create(EventDto eventDto);
	EventDto update(long id, EventDto eventDto);
	void delete(long id);
}
