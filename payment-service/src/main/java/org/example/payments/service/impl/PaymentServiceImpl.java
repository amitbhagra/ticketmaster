package org.example.payments.service.impl;

import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.example.payments.domain.Payment;
import org.example.payments.events.PaymentRequestDTO;
import org.example.payments.events.PaymentStatus;
import org.example.payments.events.PaymentStatusResponse;
import org.example.payments.mapstruct.mappers.PaymentMapper;
import org.example.payments.repository.PaymentRepository;
import org.example.payments.service.PaymentService;
import org.example.payments.web.model.PaymentDTO;
import org.example.payments.web.model.PaymentInitiateDTO;
import org.example.payments.web.model.ValidatePaymentDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    PaymentRepository paymentRepository;

    @Autowired
    KafkaTemplate<String, PaymentStatusResponse> kafkaTemplate;

    @Autowired
    PaymentMapper paymentMapper;

    @Override
    @Transactional
    public void initiatePayment(PaymentRequestDTO bookingDTO) {
        Payment payment = new Payment();
        payment.setAmount(bookingDTO.getAmount());
        payment.setBookingId(bookingDTO.getBookingId());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setOtp(String.format("%06d", (int)(Math.random() * 1000000)));
        paymentRepository.save(payment);
    }

    @Override
    @Transactional
    public PaymentStatusResponse validatePayment(ValidatePaymentDTO validatePaymentDTO) {
        Optional<Payment> paymentOpt = paymentRepository.findById(validatePaymentDTO.getId());
        PaymentStatusResponse response = new PaymentStatusResponse();

        if (paymentOpt.isPresent()) {

            Payment payment = paymentOpt.get();
            response.setBookingId(payment.getBookingId());
            payment.setPaymentMethod(validatePaymentDTO.getPaymentMethod());
            if (payment.getOtp().equals(validatePaymentDTO.getOtp())) {
                payment.setStatus(PaymentStatus.APPROVED);
                response.setPaymentStatus(PaymentStatus.APPROVED);
            } else {
                payment.setStatus(PaymentStatus.DECLINED);
                response.setPaymentStatus(PaymentStatus.DECLINED);
            }
            paymentRepository.save(payment);
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    sendMessage(response);
                }
            });
        }
        return response;
    }

    public void sendMessage(PaymentStatusResponse message) {
        kafkaTemplate.send("payment_response", message).whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Sent message=[{}] with offset=[{}]", message, result.getRecordMetadata().offset());
            } else {
                log.error("Unable to send message=[{}] due to : {}", message, ex.getMessage());
            }
        });
    }

    @Override
    public PaymentDTO getPaymentByBookingId(long bookingId) {
        Optional<Payment> paymentOpt =  paymentRepository.findByBookingId(bookingId);
        PaymentDTO paymentDTO = null;
        if (paymentOpt.isPresent()) {
            paymentDTO = paymentMapper.paymentToPaymentDto(paymentOpt.get());
        }
        return paymentDTO;
    }

    @Override
    public PaymentInitiateDTO getPaymentInitiateByBookingId(long bookingId) {
        Optional<Payment> paymentOpt =  paymentRepository.findByBookingId(bookingId);
        PaymentInitiateDTO paymentDTO = null;
        if (paymentOpt.isPresent()) {
            paymentDTO = paymentMapper.paymentToPaymentInitiateDto(paymentOpt.get());
        }
        return paymentDTO;
    }
}
