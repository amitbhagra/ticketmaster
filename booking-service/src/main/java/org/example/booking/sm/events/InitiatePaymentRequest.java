package org.example.booking.sm.events;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.booking.web.model.PaymentRequestDTO;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InitiatePaymentRequest {
	private PaymentRequestDTO paymentRequestDTO;
}
