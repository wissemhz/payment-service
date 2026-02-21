package com.localhub.paymentservice.exposition.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record ReservePaymentRequest(
        @NotNull(message = "Booking ID is required")
        Long bookingId,

        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be positive")
        BigDecimal amount,

        String paymentMethodId
) {
}
