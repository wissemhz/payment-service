package com.localhub.paymentservice.domain.exception;

public class PaymentNotFoundException extends RuntimeException {

    public static final String ERROR_CODE = "PAYMENT_NOT_FOUND";

    public PaymentNotFoundException(String message) {
        super(message);
    }
}
