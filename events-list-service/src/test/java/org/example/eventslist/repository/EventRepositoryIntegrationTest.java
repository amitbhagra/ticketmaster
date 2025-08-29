package org.example.eventslist.repository;

import org.example.eventslist.model.elasticsearch.Event;
import org.example.eventslist.service.EventGeoDistanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.domain.PageRequest;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
public class EventRepositoryIntegrationTest {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventGeoDistanceService eventGeoDistanceService;

    @Test
    void testFindByLocationNear() throws Exception {
      //  Point searchPoint = new Point(28.70410, 77.1025); // Near Central Park


        List<Event> events = eventGeoDistanceService.findEventsSortedByDistance(28.70410, 77.1025);

        assertThat(events).isNotEmpty();
        assertThat(events.stream().anyMatch(e -> "Concert".equals(e.getEventName()))).isTrue();
    }

    @Test
    void testFindAll() {
        Iterable<Event> events = eventRepository.findAll();
        // iterate all events
        assertThat(events).isNotEmpty();

    }

    @Test
    void testFindByLocationNearSortedByDistance() {
        Point searchPoint = new Point(40.780, -73.970); // Near Central Park
        Distance distance = new Distance(10, Metrics.KILOMETERS);
        PageRequest pageRequest = PageRequest.of(0, 10);

        List<Event> events = eventRepository.findByLocationNear(searchPoint, distance, pageRequest);

        assertThat(events).hasSize(2);
        assertThat(events.get(0).getEventName()).isEqualTo("Concert");
        assertThat(events.get(1).getEventName()).isEqualTo("Art Expo");
    }
}
