package org.example.payments.web.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.payments.events.PaymentMethod;
import org.example.payments.events.PaymentStatus;

@Getter
@Setter
@NoArgsConstructor
public class PaymentDTO {
    private Long bookingId;
    private Double amount;
    private PaymentStatus status;
    private PaymentMethod paymentMethod;
}
