package com.localhub.paymentservice.application.service;

import com.localhub.paymentservice.domain.model.Payment;
import com.localhub.paymentservice.domain.model.PaymentStatus;
import com.localhub.paymentservice.domain.port.PaymentPort;
import com.localhub.paymentservice.infrastructure.gateway.PaymentGatewayAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class StalePaymentCleanupTask {

    private final PaymentPort paymentPort;
    private final PaymentGatewayAdapter paymentGatewayAdapter;

    /**
     * Runs every hour. Finds payments stuck in RESERVED status for more than 24 hours
     * and releases (refunds) them.
     */
    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void cleanupStaleReservedPayments() {
        Instant cutoff = Instant.now().minus(24, ChronoUnit.HOURS);
        List<Payment> stalePayments = paymentPort.findByStatusAndReservedAtBefore(
                PaymentStatus.RESERVED, cutoff);

        if (stalePayments.isEmpty()) {
            return;
        }

        log.info("Found {} stale RESERVED payments older than 24h, releasing...", stalePayments.size());

        for (Payment payment : stalePayments) {
            try {
                paymentGatewayAdapter.refund(payment.getId(), payment.getAmount());
                payment.setStatus(PaymentStatus.REFUNDED);
                payment.setRefundedAt(Instant.now());
                payment.setRefundReason("Auto-refund: stale reservation (>24h)");
                paymentPort.save(payment);
                log.info("Stale payment {} auto-refunded for bookingId={}", payment.getId(), payment.getBookingId());
            } catch (Exception e) {
                log.error("Failed to auto-refund stale payment {}: {}", payment.getId(), e.getMessage());
            }
        }
    }
}
