package com.localhub.paymentservice.exposition.dto;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentResponse(
        UUID id,
        Long bookingId,
        Double amount,
        Double platformFee,
        Double providerPayout,
        String currency,
        String status,
        Instant reservedAt,
        Instant capturedAt,
        Instant releasedAt,
        Instant refundedAt,
        String refundReason,
        LocalDateTime createdAt
) {
}
