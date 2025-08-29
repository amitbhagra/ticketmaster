package org.example.event.web.controllers;

import java.util.List;

import jakarta.validation.Valid;

import org.example.event.web.model.VenueDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.example.event.mapstruct.mappers.EventMapper;
import org.example.event.repository.EventRepository;
import org.example.event.service.EventService;
import org.example.event.web.model.EventDto;

@RestController
@RequestMapping("/api/v1/events")
public class EventController {

	@Autowired
	EventRepository eventRepository;
	
	@Autowired
	EventService eventService;
	
	@Autowired
	EventMapper eventMapper;

	@GetMapping("")
	public ResponseEntity<List<EventDto>> getAllEvents() {
		try {
			
			List<EventDto> eventsResp = eventService.getAllEvents();

			if (eventsResp.isEmpty()) {
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}
					
			return new ResponseEntity<>(eventsResp, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/{id}")
	public ResponseEntity<EventDto> getEventById(@PathVariable("id") long id) {
		EventDto eventDto = eventService.getEventById(id);

		if (eventDto != null) {
			return new ResponseEntity<>(eventDto, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@PostMapping("")
	public ResponseEntity<EventDto> createEvent(@Valid @RequestBody EventDto eventDto) {
		try {
			EventDto eventDTO = eventService.create(eventDto);
			return new ResponseEntity<>(eventDTO, HttpStatus.CREATED);
		} catch (Exception e) {
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PutMapping("/{id}")
	public ResponseEntity<EventDto> update(@PathVariable("id") long id, @RequestBody EventDto eventDto) {
		
		eventDto = eventService.update(id, eventDto);
		
		if (eventDto != null) {
			
			return new ResponseEntity<>(eventDto, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<HttpStatus> delete(@PathVariable("id") long id) {
		try {
			EventDto existingEvent = eventService.getEventById(id);
			if (existingEvent == null) {
				return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
			}
			eventService.delete(id);
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		} catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/venues")
	public ResponseEntity<List<VenueDto>> getAllVenues() {
		try {

			List<VenueDto> venuesResp = eventService.getAllVenues();

			if (venuesResp.isEmpty()) {
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}

			return new ResponseEntity<>(venuesResp, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	

}
