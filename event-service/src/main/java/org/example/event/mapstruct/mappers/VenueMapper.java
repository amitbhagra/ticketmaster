package org.example.event.mapstruct.mappers;

import org.example.event.domain.Event;
import org.example.event.domain.Venue;
import org.example.event.web.model.EventDto;
import org.example.event.web.model.VenueDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface VenueMapper {
	
	Venue venueDtoToVenue(VenueDto venueDto);
	VenueDto venueToVenueDto(Venue venue);
	List<VenueDto> venueListToVenueDtoList(List<Venue> venues);
}
