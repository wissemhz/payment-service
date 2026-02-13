package com.localhub.paymentservice.exposition.exception;

import com.localhub.paymentservice.domain.exception.InvalidPaymentStateException;
import com.localhub.paymentservice.domain.exception.PaymentNotFoundException;
import com.localhub.paymentservice.exposition.dto.StandardErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<StandardErrorResponse> handlePaymentNotFound(PaymentNotFoundException ex) {
        log.warn("Payment not found: {}", ex.getMessage());
        StandardErrorResponse error = new StandardErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                PaymentNotFoundException.ERROR_CODE,
                ex.getMessage(),
                null
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(InvalidPaymentStateException.class)
    public ResponseEntity<StandardErrorResponse> handleInvalidPaymentState(InvalidPaymentStateException ex) {
        log.warn("Invalid payment state: {}", ex.getMessage());
        StandardErrorResponse error = new StandardErrorResponse(
                HttpStatus.CONFLICT.value(),
                InvalidPaymentStateException.ERROR_CODE,
                ex.getMessage(),
                null
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<StandardErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        log.warn("Validation error: {}", ex.getMessage());
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        StandardErrorResponse error = new StandardErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "VALIDATION_ERROR",
                "Validation failed",
                fieldErrors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<StandardErrorResponse> handleGenericException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        StandardErrorResponse error = new StandardErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "INTERNAL_ERROR",
                "An unexpected error occurred",
                null
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
