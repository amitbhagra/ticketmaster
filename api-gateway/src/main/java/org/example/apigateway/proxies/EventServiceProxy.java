package org.example.apigateway.proxies;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class EventServiceProxy {

  private final WebClient client;
  private final String eventServiceBaseUrl;

  public EventServiceProxy(@Value("${EVENT_SERVICE_URL:http://localhost:8092}") String eventServiceBaseUrl) {
    this.client = WebClient.create();
    this.eventServiceBaseUrl = eventServiceBaseUrl;
  }

  public Mono<EventInfo> findEventById(String eventId) {
    String url = eventServiceBaseUrl + "/api/v1/events/" + eventId;
    return client.get()
            .uri(url)
            .retrieve()
            .bodyToMono(EventInfo.class);
  }

  public Mono<EventInfo> findEventById(String eventId, String authHeader) {
    String url = eventServiceBaseUrl + "/api/v1/events/" + eventId;
    return client.get()
            .uri(url)
            .header("Authorization", authHeader)
            .retrieve()
            .bodyToMono(EventInfo.class);
  }
}
