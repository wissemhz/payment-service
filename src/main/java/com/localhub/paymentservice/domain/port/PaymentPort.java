package com.localhub.paymentservice.domain.port;

import com.localhub.paymentservice.domain.model.Payment;

import java.util.Optional;
import java.util.UUID;

public interface PaymentPort {

    Payment save(Payment payment);

    Optional<Payment> findById(UUID id);

    Optional<Payment> findByBookingId(Long bookingId);
}
