package org.example.payments.listeners;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.payments.events.InitiatePaymentRequest;
import org.example.payments.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
@Component
public class PaymentRequestListener {

	@Autowired
	private PaymentService paymentService;

	@KafkaListener(topics = "payment_request", containerFactory = "kafkaListenerContainerFactory")
	public void listenWithHeaders(@Payload InitiatePaymentRequest message,
			@Header(KafkaHeaders.RECEIVED_PARTITION) int partition, @Header(KafkaHeaders.OFFSET) int offset) {
		log.info("Received Message: {}, from partition: {}, from offset: {}", message, partition, offset);
		paymentService.initiatePayment(message.getPaymentRequestDTO());
	}

}
