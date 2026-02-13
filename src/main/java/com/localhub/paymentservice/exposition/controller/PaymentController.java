package com.localhub.paymentservice.exposition.controller;

import com.localhub.paymentservice.application.service.PaymentService;
import com.localhub.paymentservice.exposition.dto.PaymentResponse;
import com.localhub.paymentservice.exposition.dto.RefundRequest;
import com.localhub.paymentservice.exposition.dto.ReservePaymentRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Payment management endpoints")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/reserve")
    @Operation(summary = "Reserve a payment", description = "Creates a new payment reservation for a booking")
    public ResponseEntity<PaymentResponse> reservePayment(@Valid @RequestBody ReservePaymentRequest request) {
        PaymentResponse response = paymentService.reservePayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/capture")
    @Operation(summary = "Capture a payment", description = "Captures a previously reserved payment")
    public ResponseEntity<PaymentResponse> capturePayment(@PathVariable UUID id) {
        PaymentResponse response = paymentService.capturePayment(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/release")
    @Operation(summary = "Release a payment", description = "Releases a captured payment to the service provider")
    public ResponseEntity<PaymentResponse> releasePayment(@PathVariable UUID id) {
        PaymentResponse response = paymentService.releasePayment(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/refund")
    @Operation(summary = "Refund a payment", description = "Refunds a reserved or captured payment")
    public ResponseEntity<PaymentResponse> refundPayment(
            @PathVariable UUID id,
            @Valid @RequestBody RefundRequest request) {
        PaymentResponse response = paymentService.refundPayment(id, request.reason());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/booking/{bookingId}")
    @Operation(summary = "Get payment by booking ID", description = "Retrieves payment details for a specific booking")
    public ResponseEntity<PaymentResponse> getPaymentByBookingId(@PathVariable Long bookingId) {
        PaymentResponse response = paymentService.getPaymentByBookingId(bookingId);
        return ResponseEntity.ok(response);
    }
}
