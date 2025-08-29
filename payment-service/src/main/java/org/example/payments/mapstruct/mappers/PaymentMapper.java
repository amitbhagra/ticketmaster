package org.example.payments.mapstruct.mappers;

import org.example.payments.domain.Payment;
import org.example.payments.web.model.PaymentDTO;
import org.example.payments.web.model.PaymentInitiateDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PaymentMapper {
	
	Payment paymentDtoToPayment(PaymentDTO paymentDTO);
	PaymentDTO paymentToPaymentDto(Payment payment);
	PaymentInitiateDTO paymentToPaymentInitiateDto(Payment payment);

}
