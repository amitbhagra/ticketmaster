package org.example.event.config;

import org.example.event.domain.Venue;
import org.example.event.repository.VenueRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
public class VenueDataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(VenueDataInitializer.class);

    private final VenueRepository venueRepository;
    private final Resource venueResource;

    public VenueDataInitializer(
            VenueRepository venueRepository,
            @Value("classpath:venues.txt") Resource venueResource) {
        this.venueRepository = venueRepository;
        this.venueResource = venueResource;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (venueRepository.count() > 0) {
            log.info("Venue table already contains data. Skipping startup venue seeding.");
            return;
        }

        List<Venue> venues = loadVenues();
        venueRepository.saveAll(venues);
        log.info("Inserted {} venues from venues.txt during application startup.", venues.size());
    }

    List<Venue> loadVenues() {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(venueResource.getInputStream(), StandardCharsets.UTF_8))) {

            List<Venue> venues = new ArrayList<>();
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                String trimmedLine = line.trim();
                if (trimmedLine.isEmpty()) {
                    continue;
                }

                String[] columns = trimmedLine.split("\\t");
                if (columns.length != 3) {
                    throw new IllegalStateException("Invalid venue entry at line " + lineNumber + " in venues.txt");
                }

                Venue venue = new Venue();
                venue.setName(columns[0].trim());
                venue.setLat(parseCoordinate(columns[1], "latitude", lineNumber));
                venue.setLon(parseCoordinate(columns[2], "longitude", lineNumber));
                venues.add(venue);
            }
            return venues;
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to read venues.txt for startup seeding", ex);
        }
    }

    private double parseCoordinate(String value, String coordinateName, int lineNumber) {
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException ex) {
            throw new IllegalStateException(
                    "Invalid " + coordinateName + " at line " + lineNumber + " in venues.txt", ex);
        }
    }
}
