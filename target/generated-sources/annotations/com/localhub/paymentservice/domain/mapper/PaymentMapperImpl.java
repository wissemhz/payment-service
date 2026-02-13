package com.localhub.paymentservice.domain.mapper;

import com.localhub.paymentservice.domain.model.Payment;
import com.localhub.paymentservice.exposition.dto.PaymentResponse;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-02-13T21:14:39+0100",
    comments = "version: 1.5.3.Final, compiler: javac, environment: Java 17.0.14 (Homebrew)"
)
@Component
public class PaymentMapperImpl implements PaymentMapper {

    @Override
    public PaymentResponse toResponse(Payment payment) {
        if ( payment == null ) {
            return null;
        }

        UUID id = null;
        Long bookingId = null;
        Double amount = null;
        Double platformFee = null;
        Double providerPayout = null;
        String currency = null;
        Instant reservedAt = null;
        Instant capturedAt = null;
        Instant releasedAt = null;
        Instant refundedAt = null;
        String refundReason = null;
        LocalDateTime createdAt = null;

        id = payment.getId();
        bookingId = payment.getBookingId();
        amount = payment.getAmount();
        platformFee = payment.getPlatformFee();
        providerPayout = payment.getProviderPayout();
        currency = payment.getCurrency();
        reservedAt = payment.getReservedAt();
        capturedAt = payment.getCapturedAt();
        releasedAt = payment.getReleasedAt();
        refundedAt = payment.getRefundedAt();
        refundReason = payment.getRefundReason();
        createdAt = payment.getCreatedAt();

        String status = payment.getStatus().name();

        PaymentResponse paymentResponse = new PaymentResponse( id, bookingId, amount, platformFee, providerPayout, currency, status, reservedAt, capturedAt, releasedAt, refundedAt, refundReason, createdAt );

        return paymentResponse;
    }
}
