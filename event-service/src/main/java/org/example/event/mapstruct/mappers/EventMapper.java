package org.example.event.mapstruct.mappers;

import java.util.List;

import org.example.event.domain.Venue;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import org.example.event.domain.Event;
import org.example.event.web.model.EventDto;

@Mapper(componentModel = "spring")
public interface EventMapper {

	default Venue map(Long value) {
		if (value == null) return null;
		Venue venue = new Venue();
		venue.setId(value);
		return venue;
	}

	@Mapping(target = "venue", source = "eventDto.venue")
	Event eventDtoToEvent(EventDto eventDto);
	@Mapping(target = "venue", source = "event.venue.name")
	EventDto eventToEventDto(Event event);
	@Mapping(target = "venue", source = "event.venue.name")
	List<EventDto> eventListToEventDtoList(List<Event> events);
}
