package com.localhub.paymentservice.infrastructure.adapter.repository;

import com.localhub.paymentservice.domain.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaPaymentRepository extends JpaRepository<Payment, UUID> {

    Optional<Payment> findByBookingId(Long bookingId);
}
