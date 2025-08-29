package org.example.booking.listener;

import org.example.booking.service.impl.BookingSagaManagerImpl;
import org.example.booking.sm.events.PaymentStatusResponse;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
@Component
public class PaymentResponseListener {

	private final BookingSagaManagerImpl bookingSagaManager;

	@KafkaListener(topics = "payment_response", containerFactory = "kafkaListenerContainerFactory")
	public void listenWithHeaders(@Payload PaymentStatusResponse message,
			@Header(KafkaHeaders.RECEIVED_PARTITION) int partition, @Header(KafkaHeaders.OFFSET) int offset) {
		log.info("Received Message: {}, from partition: {}, from offset: {}", message, partition, offset);
		bookingSagaManager.processPaymentResult(message.getBookingId(), message.getPaymentStatus());
	}

}
