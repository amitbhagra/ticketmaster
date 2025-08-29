package org.example.apigateway.proxies;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PaymentInfo {
    private Long bookingId;
    private Double amount;
    private PaymentStatus status;
    private PaymentMethod paymentMethod;

}
