package org.example.payments.web.model;

import jakarta.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.payments.events.PaymentMethod;

@Getter
@Setter
@NoArgsConstructor
public class ValidatePaymentDTO {
	private Long id;
	private String otp;
	private PaymentMethod paymentMethod;
}
