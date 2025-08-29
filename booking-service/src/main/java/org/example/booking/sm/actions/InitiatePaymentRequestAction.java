package org.example.booking.sm.actions;

import java.util.Optional;

import org.example.booking.domain.Booking;
import org.example.booking.domain.BookingEvent;
import org.example.booking.domain.BookingStatus;
import org.example.booking.mappers.BookingMapper;
import org.example.booking.repository.BookingRepository;
import org.example.booking.sm.events.InitiatePaymentRequest;
import org.example.booking.web.model.PaymentRequestDTO;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class InitiatePaymentRequestAction implements Action<BookingStatus, BookingEvent> {

    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;
    private final KafkaTemplate<String, InitiatePaymentRequest> kafkaTemplate;

    @Override
    public void execute(StateContext<BookingStatus, BookingEvent> context) {
        String bookingId = (String) context.getMessage().getHeaders().get("BOOKING_ID_HEADER");
        Optional<Booking> bookingOptional = Optional.empty();
        if (bookingId != null) {
            bookingOptional = bookingRepository.findById(Long.valueOf(bookingId));
        }

        bookingOptional.ifPresentOrElse(booking -> sendMessage(InitiatePaymentRequest.builder()
                .paymentRequestDTO(PaymentRequestDTO.builder().bookingId(booking.getId()).amount(booking.getAmount())
                .build()).build()), () -> log.error("Booking Not Found. Id: {}", bookingId));

        log.debug("Sent Payment request to queue for booking id {}", bookingId);
    }

    public void sendMessage(InitiatePaymentRequest message) {
        kafkaTemplate.send("payment_request", message).whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Sent message=[{}] with offset=[{}]", message, result.getRecordMetadata().offset());
            } else {
                log.error("Unable to send message=[{}] due to : {}", message, ex.getMessage());
            }
        });
    }
}