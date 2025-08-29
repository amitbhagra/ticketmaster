package org.example.payments.web.controllers;

import java.util.List;

import jakarta.validation.Valid;

import org.example.payments.events.PaymentStatusResponse;
import org.example.payments.service.PaymentService;
import org.example.payments.web.model.PaymentDTO;
import org.example.payments.web.model.PaymentInitiateDTO;
import org.example.payments.web.model.ValidatePaymentDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentsController {


    @Autowired
    PaymentService paymentService;


    @GetMapping("/{bookingId}")
    public ResponseEntity<PaymentDTO> getPaymentByBookingId(@PathVariable("bookingId") long bookingId) {
        PaymentDTO paymentDTO = paymentService.getPaymentByBookingId(bookingId);

        if (paymentDTO != null) {
            return new ResponseEntity<>(paymentDTO, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/initiate/{bookingId}")
    public ResponseEntity<PaymentInitiateDTO> getPaymentInitiationByBookingId(@PathVariable("bookingId") long bookingId) {
        PaymentInitiateDTO paymentDTO = paymentService.getPaymentInitiateByBookingId(bookingId);

        if (paymentDTO != null) {
            return new ResponseEntity<>(paymentDTO, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/validate")
    public ResponseEntity<PaymentStatusResponse> validatePayment(@Valid @RequestBody ValidatePaymentDTO validatePaymentDTO) {
        try {
            PaymentStatusResponse response = paymentService.validatePayment(validatePaymentDTO);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}
