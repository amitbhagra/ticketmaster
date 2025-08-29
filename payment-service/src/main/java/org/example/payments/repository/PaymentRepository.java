package org.example.payments.repository;

import org.example.payments.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface PaymentRepository extends JpaRepository<Payment, Long> {
    // Custom query method to find a payment by its booking ID return optional
    Optional<Payment> findByBookingId(Long bookingId);
}
