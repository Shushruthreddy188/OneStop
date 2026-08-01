package com.onestop.payment.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public final class PaymentDtos {

    private PaymentDtos() {
    }

    /** Internal: order-service asks us to take payment for an order. */
    public record ProcessPaymentRequest(
            @NotNull Long orderId,
            @NotNull Long customerId,
            @NotNull @Positive BigDecimal amount,
            String currency,
            @NotBlank String method) {
    }

    public record PaymentResult(
            Long paymentId,
            boolean success,
            String status,
            String provider,
            String providerRef,
            String message) {
    }

    public record PaymentDto(
            Long id,
            Long orderId,
            BigDecimal amount,
            String currency,
            String method,
            String status,
            String provider,
            String providerRef,
            OffsetDateTime createdAt) {
    }
}
