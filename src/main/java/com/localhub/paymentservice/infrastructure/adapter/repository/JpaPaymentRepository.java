package com.localhub.paymentservice.infrastructure.adapter.repository;

import com.localhub.paymentservice.domain.model.Payment;
import com.localhub.paymentservice.domain.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaPaymentRepository extends JpaRepository<Payment, UUID> {

    Optional<Payment> findByBookingId(Long bookingId);

    List<Payment> findByStatusAndReservedAtBefore(PaymentStatus status, Instant cutoff);
}
