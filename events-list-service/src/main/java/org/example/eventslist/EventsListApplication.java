package org.example.eventslist;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration;

@SpringBootApplication(exclude = {ElasticsearchDataAutoConfiguration.class})
public class EventsListApplication {

    private static final Logger log = LoggerFactory.getLogger(EventsListApplication.class);

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(EventsListApplication.class);
        app.addListeners((ApplicationEnvironmentPreparedEvent event) -> {
            log.info("Effective spring.elasticsearch.uris: {}", sanitizeUris(event.getEnvironment().getProperty("spring.elasticsearch.uris", "<not-set>")));
            log.info("Effective opensearch.uris: {}", sanitizeUris(event.getEnvironment().getProperty("opensearch.uris", "<not-set>")));
        });
        app.run(args);
    }

    private static String sanitizeUris(String uris) {
        // Mask credentials if URI contains user:password@host
        return uris.replaceAll("(https?://)([^/@:]+):([^/@]+)@", "$1$2:****@");
    }
}
