package org.example.booking.service.impl;

import java.util.Optional;

import org.example.booking.domain.Booking;
import org.example.booking.domain.BookingEvent;
import org.example.booking.domain.BookingStatus;
import org.example.booking.domain.PaymentStatus;
import org.example.booking.repository.BookingRepository;
import org.example.booking.service.BookingSagaManager;
import org.example.booking.sm.BookingStateChangeInterceptor;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class BookingSagaManagerImpl implements BookingSagaManager {

    private final StateMachineFactory<BookingStatus, BookingEvent> stateMachineFactory;
    private final BookingStateChangeInterceptor stateChangeInterceptor;
    private final BookingRepository bookingRepository;


    @Override
    @Transactional
    public Booking newBooking(Booking booking) {
        booking.setStatus(BookingStatus.NEW);
        booking.setId(null);


        Booking savedBooking = bookingRepository.save(booking);
        sendEvent(savedBooking, BookingEvent.PAYMENT_INITIATED);
        return savedBooking;
    }

    private StateMachine<BookingStatus, BookingEvent> build(Booking booking) {
        StateMachine<BookingStatus, BookingEvent> sm = stateMachineFactory.getStateMachine(String.valueOf(booking.getId()));

        sm.stop();

        sm.getStateMachineAccessor()
                .doWithAllRegions(sma -> {
                    sma.addStateMachineInterceptor(stateChangeInterceptor);
                    sma.resetStateMachine(new DefaultStateMachineContext<>(booking.getStatus(), null, null, null));
                });

        sm.start();

        return sm;
    }

    private void sendEvent(Booking booking, BookingEvent eventEnum) {
        StateMachine<BookingStatus, BookingEvent> sm = build(booking);

        Message<BookingEvent> msg = MessageBuilder.withPayload(eventEnum)
                .setHeader("BOOKING_ID_HEADER", booking.getId().toString())
                .build();

        sm.sendEvent(msg);
    }


    @Override
    public void processPaymentResult(Long bookingId, PaymentStatus status) {
        log.info("Process Payment Result for Booking Id: {} Status {}", bookingId, status.toString());

        Optional<Booking> bookingOptional = bookingRepository.findById(bookingId);

        bookingOptional.ifPresentOrElse(booking -> {
            if (status == PaymentStatus.APPROVED) {
                sendEvent(booking, BookingEvent.PAYMENT_COMPLETED);

            } else {
                sendEvent(booking, BookingEvent.PAYMENT_FAILED);
            }
        }, () -> log.error("Booking Not Found. Id: " + bookingId));

    }

}
