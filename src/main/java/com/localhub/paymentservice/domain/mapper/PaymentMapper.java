package com.localhub.paymentservice.domain.mapper;

import com.localhub.paymentservice.domain.model.Payment;
import com.localhub.paymentservice.exposition.dto.PaymentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(target = "status", expression = "java(payment.getStatus().name())")
    PaymentResponse toResponse(Payment payment);
}
