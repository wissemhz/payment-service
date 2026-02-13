package com.localhub.paymentservice.exposition.dto;

import jakarta.validation.constraints.NotBlank;

public record RefundRequest(
        @NotBlank(message = "Refund reason is required")
        String reason
) {
}
