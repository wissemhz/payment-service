package com.localhub.paymentservice.infrastructure.gateway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Mock payment gateway adapter for MVP.
 * In production, this would integrate with a real payment provider (e.g., Stripe, PayPal).
 */
@Service
@Slf4j
public class PaymentGatewayAdapter {

    public boolean reserve(BigDecimal amount, String paymentMethodId) {
        log.info("Mock gateway: reserving amount={} with paymentMethodId={}", amount, paymentMethodId);
        return true;
    }

    public boolean capture(UUID paymentId, BigDecimal amount) {
        log.info("Mock gateway: capturing paymentId={} amount={}", paymentId, amount);
        return true;
    }

    public boolean release(UUID paymentId, BigDecimal amount) {
        log.info("Mock gateway: releasing paymentId={} amount={}", paymentId, amount);
        return true;
    }

    public boolean refund(UUID paymentId, BigDecimal amount) {
        log.info("Mock gateway: refunding paymentId={} amount={}", paymentId, amount);
        return true;
    }
}
