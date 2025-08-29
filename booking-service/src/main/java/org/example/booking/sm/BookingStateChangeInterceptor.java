package org.example.booking.sm;

import java.util.Optional;

import org.example.booking.domain.BookingEvent;
import org.example.booking.domain.BookingStatus;
import org.example.booking.repository.BookingRepository;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.transition.Transition;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingStateChangeInterceptor extends StateMachineInterceptorAdapter<BookingStatus, BookingEvent> {

    private final BookingRepository bookingRepository;

    @Transactional
    @Override
    public void preStateChange(State<BookingStatus, BookingEvent> state, Message<BookingEvent> message, Transition<BookingStatus, BookingEvent> transition, StateMachine<BookingStatus, BookingEvent> stateMachine, StateMachine<BookingStatus, BookingEvent> rootStateMachine) {
        log.debug("Pre-State Change");

        Optional.ofNullable(message).flatMap(msg -> Optional.ofNullable((String) msg.getHeaders().getOrDefault("BOOKING_ID_HEADER", " "))).ifPresent(bookingId -> {
            log.debug("Saving state for booking id: {}, Status: {}", bookingId, state.getId());
            bookingRepository.findById(Long.valueOf(bookingId)).ifPresent(booking -> {
                booking.setStatus(state.getId());
                bookingRepository.save(booking);
            });
        });
    }
}
