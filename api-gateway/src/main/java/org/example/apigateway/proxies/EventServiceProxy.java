package org.example.apigateway.proxies;


import org.springframework.stereotype.Service;

import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class EventServiceProxy {

  private final WebClient client;

  public EventServiceProxy() {
    this.client = WebClient.create();
  }

  public Mono<EventInfo> findEventById(String eventId) {
    String url = "http://localhost:8092/api/v1/events/" + eventId;
    return client.get()
            .uri(url)
            .retrieve()
            .bodyToMono(EventInfo.class);
  }

  public Mono<EventInfo> findEventById(String eventId, String authHeader) {
    String url = "http://localhost:8092/api/v1/events/" + eventId;
    return client.get()
            .uri(url)
            .header("Authorization", authHeader)
            .retrieve()
            .bodyToMono(EventInfo.class);
  }
}
