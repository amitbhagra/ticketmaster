package org.example.payments.web.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PaymentInitiateDTO {
    private Long id;
    private Long bookingId;
    private String otp;
}
