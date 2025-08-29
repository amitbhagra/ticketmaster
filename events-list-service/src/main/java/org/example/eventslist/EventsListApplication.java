package org.example.eventslist;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration;

@SpringBootApplication(exclude = {ElasticsearchDataAutoConfiguration.class})
public class EventsListApplication {

    public static void main(String[] args) {
        SpringApplication.run(EventsListApplication.class, args);
    }
}
