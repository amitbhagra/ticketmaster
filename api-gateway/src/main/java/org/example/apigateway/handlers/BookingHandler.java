package org.example.apigateway.handlers;


import static org.springframework.web.reactive.function.BodyInserters.fromObject;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.example.apigateway.proxies.*;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;


import reactor.core.publisher.Mono;
import reactor.util.function.Tuple3;

@Component
@RequiredArgsConstructor
public class BookingHandler {

  private final BookingServiceProxy bookingService;
  private final EventServiceProxy eventService;
  private final PaymentServiceProxy paymentService;


  public Mono<ServerResponse> getBookingDetails(ServerRequest serverRequest) {
    String bookingId = serverRequest.pathVariable("bookingId");
    String authHeader = serverRequest.headers().firstHeader("Authorization");

    return bookingService.findBookingById(bookingId, authHeader)
            .flatMap(bookingInfo -> {
              Mono<Optional<PaymentInfo>> paymentInfoMono = paymentService
                      .findPaymentByBookingId(bookingId, authHeader)
                      .map(Optional::of)
                      .onErrorReturn(Optional.empty());

              Mono<Optional<EventInfo>> eventInfoMono = eventService
                      .findEventById(String.valueOf(bookingInfo.getEventId()), authHeader)
                      .map(Optional::of)
                      .onErrorReturn(Optional.empty());

              return Mono.zip(Mono.just(bookingInfo), paymentInfoMono, eventInfoMono)
                      .map(BookingDetails::makeBookingDetails)
                      .flatMap(details -> ServerResponse.ok()
                              .contentType(MediaType.APPLICATION_JSON)
                              .body(fromObject(details)));
            })
            .onErrorResume(BookingNotFoundException.class, e -> ServerResponse.notFound().build());
  }


}