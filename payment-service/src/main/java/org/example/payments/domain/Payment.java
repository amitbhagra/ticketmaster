package org.example.payments.domain;

import jakarta.persistence.Entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.payments.events.PaymentMethod;
import org.example.payments.events.PaymentStatus;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Payment extends BaseEntity {

	private Long bookingId;
	private Double amount;
	private PaymentStatus status;
	private PaymentMethod paymentMethod;
	private String otp;

}
