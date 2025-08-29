package org.example.booking.sm;

import java.util.EnumSet;

import org.example.booking.domain.BookingEvent;
import org.example.booking.domain.BookingStatus;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;


import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Configuration
@EnableStateMachineFactory
public class BookingStateMachineConfig extends StateMachineConfigurerAdapter<BookingStatus, BookingEvent> {

    private final Action<BookingStatus, BookingEvent> initiatePaymentRequestAction;

    @Override
    public void configure(StateMachineStateConfigurer<BookingStatus, BookingEvent> states) throws Exception {
        states.withStates()
                .initial(BookingStatus.NEW)
                .states(EnumSet.allOf(BookingStatus.class))
                .end(BookingStatus.CONFIRMED)
                .end(BookingStatus.CANCELLED)
                .end(BookingStatus.FAILED)
                .end(BookingStatus.REFUNDED);
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<BookingStatus, BookingEvent> transitions) throws Exception {
        transitions.withExternal()
                .source(BookingStatus.NEW).target(BookingStatus.PAYMENT_PENDING)
                .event(BookingEvent.PAYMENT_INITIATED)
                .action(initiatePaymentRequestAction)
                .and().withExternal()
                .source(BookingStatus.PAYMENT_PENDING).target(BookingStatus.CONFIRMED)
                .event(BookingEvent.PAYMENT_COMPLETED)
                .and().withExternal()
                .source(BookingStatus.PAYMENT_PENDING).target(BookingStatus.FAILED)
                .event(BookingEvent.PAYMENT_FAILED);
    }
}
