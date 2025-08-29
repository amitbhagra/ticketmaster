package org.example.apigateway.proxies;


import org.springframework.stereotype.Service;

import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class PaymentServiceProxy {

  private final WebClient client;

  public PaymentServiceProxy() {
    this.client = WebClient.create();
  }

  public Mono<PaymentInfo> findPaymentByBookingId(String bookingId) {
    String url = "http://localhost:8096/api/v1/payments/" + bookingId;
    return client.get()
            .uri(url)
            .retrieve()
            .bodyToMono(PaymentInfo.class);
  }

  public Mono<PaymentInfo> findPaymentByBookingId(String bookingId, String authHeader) {
    String url = "http://localhost:8096/api/v1/payments/" + bookingId;
    return client.get()
            .uri(url)
            .header("Authorization", authHeader)
            .retrieve()
            .bodyToMono(PaymentInfo.class);
  }
}
