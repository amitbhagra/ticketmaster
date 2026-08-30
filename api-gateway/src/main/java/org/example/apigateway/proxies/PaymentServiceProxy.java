package org.example.apigateway.proxies;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class PaymentServiceProxy {

  private final WebClient client;
  private final String paymentServiceBaseUrl;

  public PaymentServiceProxy(@Value("${PAYMENT_SERVICE_URL:http://localhost:8096}") String paymentServiceBaseUrl) {
    this.client = WebClient.create();
    this.paymentServiceBaseUrl = paymentServiceBaseUrl;
  }

  public Mono<PaymentInfo> findPaymentByBookingId(String bookingId) {
    String url = paymentServiceBaseUrl + "/api/v1/payments/" + bookingId;
    return client.get()
            .uri(url)
            .retrieve()
            .bodyToMono(PaymentInfo.class);
  }

  public Mono<PaymentInfo> findPaymentByBookingId(String bookingId, String authHeader) {
    String url = paymentServiceBaseUrl + "/api/v1/payments/" + bookingId;
    return client.get()
            .uri(url)
            .header("Authorization", authHeader)
            .retrieve()
            .bodyToMono(PaymentInfo.class);
  }
}
