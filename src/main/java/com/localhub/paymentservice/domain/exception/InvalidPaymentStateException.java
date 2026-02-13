package com.localhub.paymentservice.domain.exception;

public class InvalidPaymentStateException extends RuntimeException {

    public static final String ERROR_CODE = "INVALID_PAYMENT_STATE";

    public InvalidPaymentStateException(String message) {
        super(message);
    }
}
