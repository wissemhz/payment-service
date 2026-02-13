package com.localhub.paymentservice.infrastructure.adapter.repository;

import com.localhub.paymentservice.domain.model.Payment;
import com.localhub.paymentservice.domain.port.PaymentPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PaymentAdapter implements PaymentPort {

    private final JpaPaymentRepository jpaPaymentRepository;

    @Override
    public Payment save(Payment payment) {
        return jpaPaymentRepository.save(payment);
    }

    @Override
    public Optional<Payment> findById(UUID id) {
        return jpaPaymentRepository.findById(id);
    }

    @Override
    public Optional<Payment> findByBookingId(Long bookingId) {
        return jpaPaymentRepository.findByBookingId(bookingId);
    }
}
