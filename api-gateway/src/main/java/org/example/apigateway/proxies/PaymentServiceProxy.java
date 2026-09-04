package org.example.apigateway.proxies;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;

import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class PaymentServiceProxy {

  private final WebClient client;

  public PaymentServiceProxy(
          WebClient.Builder webClientBuilder,
          @Value("${PAYMENT_SERVICE_URL:http://localhost:8096}") String paymentServiceBaseUrl) {
    this.client = webClientBuilder
            .baseUrl(paymentServiceBaseUrl)
            .build();
  }

  public Mono<PaymentInfo> findPaymentByBookingId(String bookingId) {
    return client.get()
            .uri("/api/v1/payments/{bookingId}", bookingId)
            .exchangeToMono(resp -> {
              HttpStatusCode statusCode = resp.statusCode();

              if (statusCode.equals(HttpStatus.OK)) {
                return resp.bodyToMono(PaymentInfo.class);
              } else if (statusCode.equals(HttpStatus.NOT_FOUND)) {
                return Mono.error(new PaymentNotFoundException());
              } else {
                return Mono.error(new RuntimeException("Unknown status code: " + statusCode));
              }
            });
  }

  public Mono<PaymentInfo> findPaymentByBookingId(String bookingId, String authHeader) {
    return client.get()
            .uri("/api/v1/payments/{bookingId}", bookingId)
            .header("Authorization", authHeader)
            .exchangeToMono(resp -> {
              HttpStatusCode statusCode = resp.statusCode();

              if (statusCode.equals(HttpStatus.OK)) {
                return resp.bodyToMono(PaymentInfo.class);
              } else if (statusCode.equals(HttpStatus.NOT_FOUND)) {
                return Mono.error(new PaymentNotFoundException());
              } else {
                return Mono.error(new RuntimeException("Unknown status code: " + statusCode));
              }
            });
  }
}
