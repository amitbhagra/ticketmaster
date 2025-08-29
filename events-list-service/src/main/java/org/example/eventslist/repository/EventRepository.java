package org.example.eventslist.repository;

import org.example.eventslist.model.elasticsearch.Event;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Repository
public interface EventRepository extends ElasticsearchRepository<Event, String> {
    /**
     * Finds events near the given location within the specified distance.
     * @param location the center point (latitude, longitude)
     * @param distance the distance radius
     * @return list of events near the location
     */
    List<Event> findByLocationNear(Point location, Distance distance);

    /**
     * Finds all events sorted by their distance to the given location.
     * @param location the center point (latitude, longitude)
     * @param distance the search radius
     * @param pageable for sorting by distance
     * @return list of events sorted by distance
     */
    List<Event> findByLocationNear(Point location, Distance distance, Pageable pageable);
}
