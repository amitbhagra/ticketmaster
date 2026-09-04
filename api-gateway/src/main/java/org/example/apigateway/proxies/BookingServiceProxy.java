package org.example.apigateway.proxies;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class BookingServiceProxy {

    private final WebClient client;

    // 1. Inject the auto-configured WebClient.Builder bean
    public BookingServiceProxy(
            WebClient.Builder webClientBuilder,
            @Value("${BOOKING_SERVICE_URL:http://localhost:8094}") String bookingServiceBaseUrl) {

        // 2. Set the base URL here so the tracing filters are applied correctly
        this.client = webClientBuilder
                .baseUrl(bookingServiceBaseUrl)
                .build();
    }

    public Mono<BookingInfo> findBookingById(String bookingId, String authHeader) {
        return client
                .get()
                // 3. Use a relative path with path variables instead of string concatenation
                .uri("/api/v1/bookings/{bookingId}", bookingId)
                .header("Authorization", authHeader)
                .exchangeToMono(resp -> {
                    HttpStatusCode statusCode = resp.statusCode();

                    if (statusCode.equals(HttpStatus.OK)) {
                        return resp.bodyToMono(BookingInfo.class);
                    } else if (statusCode.equals(HttpStatus.NOT_FOUND)) {
                        return Mono.error(new BookingNotFoundException());
                    } else {
                        return Mono.error(new RuntimeException("Unknown status code: " + statusCode));
                    }
                });
    }
}

