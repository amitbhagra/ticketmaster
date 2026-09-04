package org.example.apigateway.proxies;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;

import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class EventServiceProxy {

  private final WebClient client;

  public EventServiceProxy(
          WebClient.Builder webClientBuilder,
          @Value("${EVENT_SERVICE_URL:http://localhost:8092}") String eventServiceBaseUrl) {
    this.client = webClientBuilder
            .baseUrl(eventServiceBaseUrl)
            .build();
  }

  public Mono<EventInfo> findEventById(String eventId) {
    return client.get()
            .uri("/api/v1/events/{eventId}", eventId)
            .exchangeToMono(resp -> {
              HttpStatusCode statusCode = resp.statusCode();

              if (statusCode.equals(HttpStatus.OK)) {
                return resp.bodyToMono(EventInfo.class);
              } else if (statusCode.equals(HttpStatus.NOT_FOUND)) {
                return Mono.error(new EventNotFoundException());
              } else {
                return Mono.error(new RuntimeException("Unknown status code: " + statusCode));
              }
            });
  }

  public Mono<EventInfo> findEventById(String eventId, String authHeader) {
    return client.get()
            .uri("/api/v1/events/{eventId}", eventId)
            .header("Authorization", authHeader)
            .exchangeToMono(resp -> {
              HttpStatusCode statusCode = resp.statusCode();

              if (statusCode.equals(HttpStatus.OK)) {
                return resp.bodyToMono(EventInfo.class);
              } else if (statusCode.equals(HttpStatus.NOT_FOUND)) {
                return Mono.error(new EventNotFoundException());
              } else {
                return Mono.error(new RuntimeException("Unknown status code: " + statusCode));
              }
            });
  }
}
