package org.example.payments.service;

import org.example.payments.events.PaymentRequestDTO;
import org.example.payments.events.PaymentStatusResponse;
import org.example.payments.web.model.PaymentDTO;
import org.example.payments.web.model.PaymentInitiateDTO;
import org.example.payments.web.model.ValidatePaymentDTO;

public interface PaymentService {
	void initiatePayment(PaymentRequestDTO bookingDTO);
	PaymentStatusResponse validatePayment(ValidatePaymentDTO validatePaymentDTO);

	PaymentDTO getPaymentByBookingId(long id);

	PaymentInitiateDTO getPaymentInitiateByBookingId(long bookingId);
}
