package org.example.booking.sm.events;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.booking.domain.PaymentStatus;
import org.example.booking.web.model.BookingDTO;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentStatusResponse {
	private PaymentStatus paymentStatus;
	private Long bookingId;
}
