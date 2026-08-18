package org.example.event.config;

import org.example.event.domain.Venue;
import org.example.event.repository.VenueRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.lang.NonNull;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VenueDataInitializerTest {

    @Test
    void seedsVenuesWhenDatabaseIsEmpty() {
        List<Venue> savedVenues = new ArrayList<>();
        VenueRepository venueRepository = repositoryStub(0L, savedVenues);
        Resource resource = new ByteArrayResource(("Venue One\t12.34\t56.78\nVenue Two\t21.43\t65.87")
                .getBytes(StandardCharsets.UTF_8));
        VenueDataInitializer initializer = new VenueDataInitializer(venueRepository, resource);

        initializer.run(new DefaultApplicationArguments());

        assertEquals(2, savedVenues.size());
        assertEquals("Venue One", savedVenues.getFirst().getName());
        assertEquals(12.34, savedVenues.getFirst().getLat());
        assertEquals(56.78, savedVenues.getFirst().getLon());
        assertEquals("Venue Two", savedVenues.get(1).getName());
        assertEquals(21.43, savedVenues.get(1).getLat());
        assertEquals(65.87, savedVenues.get(1).getLon());
    }

    @Test
    void skipsSeedingWhenVenuesAlreadyExist() {
        List<Venue> savedVenues = new ArrayList<>();
        VenueRepository venueRepository = repositoryStub(5L, savedVenues);
        TrackingResource resource = new TrackingResource("Venue One\t12.34\t56.78");
        VenueDataInitializer initializer = new VenueDataInitializer(venueRepository, resource);

        initializer.run(new DefaultApplicationArguments());

        assertTrue(savedVenues.isEmpty());
        assertFalse(resource.wasRead());
    }

    @Test
    void throwsHelpfulErrorForMalformedVenueLine() {
        VenueRepository venueRepository = repositoryStub(0L, new ArrayList<>());
        Resource resource = new ByteArrayResource("Broken Venue\t12.34".getBytes(StandardCharsets.UTF_8));
        VenueDataInitializer initializer = new VenueDataInitializer(venueRepository, resource);

        IllegalStateException exception = assertThrows(IllegalStateException.class, initializer::loadVenues);
        assertTrue(exception.getMessage().contains("line 1"));
    }

    private VenueRepository repositoryStub(long count, List<Venue> savedVenues) {
        return (VenueRepository) Proxy.newProxyInstance(
                VenueRepository.class.getClassLoader(),
                new Class[]{VenueRepository.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "count" -> count;
                    case "saveAll" -> {
                        savedVenues.clear();
                        Iterable<?> iterable = (Iterable<?>) args[0];
                        for (Object item : iterable) {
                            savedVenues.add((Venue) item);
                        }
                        yield args[0];
                    }
                    case "toString" -> "VenueRepositoryStub";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    default -> throw new UnsupportedOperationException("Unexpected repository method: " + method.getName());
                }
        );
    }

    private static final class TrackingResource extends AbstractResource {

        private final byte[] data;
        private boolean read;

        private TrackingResource(String content) {
            this.data = content.getBytes(StandardCharsets.UTF_8);
        }

        @Override
        public @NonNull String getDescription() {
            return "tracking-resource";
        }

        @Override
        public @NonNull InputStream getInputStream() {
            read = true;
            return new ByteArrayInputStream(data);
        }

        boolean wasRead() {
            return read;
        }
    }
}
