package org.example.apigateway.config;

import org.example.apigateway.handlers.BookingHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class BookingConfig {
    @Bean
    public RouterFunction<ServerResponse> bookingHandlerRouting(BookingHandler bookingHandler) {
        return RouterFunctions.route(
                RequestPredicates.GET("/api/v1/bookings/{bookingId}"),
                bookingHandler::getBookingDetails
        );
    }

}
