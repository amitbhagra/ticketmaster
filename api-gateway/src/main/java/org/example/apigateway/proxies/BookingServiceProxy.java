package org.example.apigateway.proxies;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

@Service
public class BookingServiceProxy {

  private final WebClient client;

  public BookingServiceProxy() {
    this.client = WebClient.create();
  }

  public Mono<BookingInfo> findBookingById(String bookingId) {
    return client
            .get()
            .uri("http://localhost:8094/api/v1/bookings/" + bookingId)
            .exchangeToMono(resp -> switch (resp.statusCode()) {
              case OK -> resp.bodyToMono(BookingInfo.class);
              case NOT_FOUND -> Mono.error(new BookingNotFoundException());
              default -> Mono.error(new RuntimeException("Unknown" + resp.statusCode()));
            });
  }

  public Mono<BookingInfo> findBookingById(String bookingId, String authHeader) {
    return client
            .get()
            .uri("http://localhost:8094/api/v1/bookings/" + bookingId)
            .header("Authorization", authHeader)
            .exchangeToMono(resp -> switch (resp.statusCode()) {
              case OK -> resp.bodyToMono(BookingInfo.class);
              case NOT_FOUND -> Mono.error(new BookingNotFoundException());
              default -> Mono.error(new RuntimeException("Unknown" + resp.statusCode()));
            });
  }


}
