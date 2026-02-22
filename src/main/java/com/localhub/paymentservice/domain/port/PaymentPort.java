package com.localhub.paymentservice.domain.port;

import com.localhub.paymentservice.domain.model.Payment;
import com.localhub.paymentservice.domain.model.PaymentStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentPort {

    Payment save(Payment payment);

    Optional<Payment> findById(UUID id);

    Optional<Payment> findByBookingId(Long bookingId);

    List<Payment> findByStatusAndReservedAtBefore(PaymentStatus status, Instant cutoff);
}
