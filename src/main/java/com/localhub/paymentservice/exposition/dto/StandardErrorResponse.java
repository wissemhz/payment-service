package com.localhub.paymentservice.exposition.dto;

public record StandardErrorResponse(
        int status,
        String code,
        String message,
        Object details
) {
}
