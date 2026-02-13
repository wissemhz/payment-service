package com.localhub.paymentservice.application.service;

import com.localhub.paymentservice.domain.exception.InvalidPaymentStateException;
import com.localhub.paymentservice.domain.exception.PaymentNotFoundException;
import com.localhub.paymentservice.domain.mapper.PaymentMapper;
import com.localhub.paymentservice.domain.model.Payment;
import com.localhub.paymentservice.domain.model.PaymentStatus;
import com.localhub.paymentservice.domain.port.PaymentPort;
import com.localhub.paymentservice.exposition.dto.PaymentResponse;
import com.localhub.paymentservice.exposition.dto.ReservePaymentRequest;
import com.localhub.paymentservice.infrastructure.gateway.PaymentGatewayAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PaymentService {

    private final PaymentPort paymentPort;
    private final PaymentMapper paymentMapper;
    private final PaymentGatewayAdapter paymentGatewayAdapter;

    /**
     * Reserve a payment for a booking.
     * Platform fee = 10% of amount, provider payout = amount - platform fee.
     */
    public PaymentResponse reservePayment(ReservePaymentRequest request) {
        log.info("Reserving payment for bookingId={} amount={}", request.bookingId(), request.amount());

        // Call mock payment gateway to reserve
        boolean reserved = paymentGatewayAdapter.reserve(request.amount(), request.paymentMethodId());
        if (!reserved) {
            throw new InvalidPaymentStateException("Payment gateway failed to reserve payment");
        }

        double platformFee = request.amount() * 0.10;
        double providerPayout = request.amount() - platformFee;

        Payment payment = Payment.builder()
                .bookingId(request.bookingId())
                .amount(request.amount())
                .platformFee(platformFee)
                .providerPayout(providerPayout)
                .currency("EUR")
                .status(PaymentStatus.RESERVED)
                .paymentMethodId(request.paymentMethodId())
                .reservedAt(Instant.now())
                .build();

        Payment saved = paymentPort.save(payment);
        log.info("Payment reserved successfully: id={}", saved.getId());
        return paymentMapper.toResponse(saved);
    }

    /**
     * Capture a reserved payment.
     * Transition: RESERVED -> CAPTURED.
     * Idempotent: if already CAPTURED, return success.
     */
    public PaymentResponse capturePayment(UUID id) {
        log.info("Capturing payment id={}", id);

        Payment payment = paymentPort.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with id: " + id));

        // Idempotency: already in target state
        if (payment.getStatus() == PaymentStatus.CAPTURED) {
            log.info("Payment id={} is already CAPTURED, returning idempotent response", id);
            return paymentMapper.toResponse(payment);
        }

        if (payment.getStatus() != PaymentStatus.RESERVED) {
            throw new InvalidPaymentStateException(
                    "Cannot capture payment in state " + payment.getStatus() + ". Expected: RESERVED");
        }

        // Call mock payment gateway to capture
        boolean captured = paymentGatewayAdapter.capture(id, payment.getAmount());
        if (!captured) {
            throw new InvalidPaymentStateException("Payment gateway failed to capture payment");
        }

        payment.setStatus(PaymentStatus.CAPTURED);
        payment.setCapturedAt(Instant.now());

        Payment saved = paymentPort.save(payment);
        log.info("Payment captured successfully: id={}", saved.getId());
        return paymentMapper.toResponse(saved);
    }

    /**
     * Release a captured payment (payout to provider).
     * Transition: CAPTURED -> RELEASED.
     * Idempotent: if already RELEASED, return success.
     */
    public PaymentResponse releasePayment(UUID id) {
        log.info("Releasing payment id={}", id);

        Payment payment = paymentPort.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with id: " + id));

        // Idempotency: already in target state
        if (payment.getStatus() == PaymentStatus.RELEASED) {
            log.info("Payment id={} is already RELEASED, returning idempotent response", id);
            return paymentMapper.toResponse(payment);
        }

        if (payment.getStatus() != PaymentStatus.CAPTURED) {
            throw new InvalidPaymentStateException(
                    "Cannot release payment in state " + payment.getStatus() + ". Expected: CAPTURED");
        }

        // Call mock payment gateway to release
        boolean released = paymentGatewayAdapter.release(id, payment.getAmount());
        if (!released) {
            throw new InvalidPaymentStateException("Payment gateway failed to release payment");
        }

        payment.setStatus(PaymentStatus.RELEASED);
        payment.setReleasedAt(Instant.now());

        Payment saved = paymentPort.save(payment);
        log.info("Payment released successfully: id={}", saved.getId());
        return paymentMapper.toResponse(saved);
    }

    /**
     * Refund a payment.
     * Transition: RESERVED or CAPTURED -> REFUNDED.
     * Idempotent: if already REFUNDED, return success.
     */
    public PaymentResponse refundPayment(UUID id, String reason) {
        log.info("Refunding payment id={} reason={}", id, reason);

        Payment payment = paymentPort.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with id: " + id));

        // Idempotency: already in target state
        if (payment.getStatus() == PaymentStatus.REFUNDED) {
            log.info("Payment id={} is already REFUNDED, returning idempotent response", id);
            return paymentMapper.toResponse(payment);
        }

        if (payment.getStatus() != PaymentStatus.RESERVED && payment.getStatus() != PaymentStatus.CAPTURED) {
            throw new InvalidPaymentStateException(
                    "Cannot refund payment in state " + payment.getStatus() + ". Expected: RESERVED or CAPTURED");
        }

        // Call mock payment gateway to refund
        boolean refunded = paymentGatewayAdapter.refund(id, payment.getAmount());
        if (!refunded) {
            throw new InvalidPaymentStateException("Payment gateway failed to refund payment");
        }

        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setRefundedAt(Instant.now());
        payment.setRefundReason(reason);

        Payment saved = paymentPort.save(payment);
        log.info("Payment refunded successfully: id={}", saved.getId());
        return paymentMapper.toResponse(saved);
    }

    /**
     * Get payment by booking ID.
     */
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByBookingId(Long bookingId) {
        log.info("Fetching payment for bookingId={}", bookingId);

        Payment payment = paymentPort.findByBookingId(bookingId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found for bookingId: " + bookingId));

        return paymentMapper.toResponse(payment);
    }
}
