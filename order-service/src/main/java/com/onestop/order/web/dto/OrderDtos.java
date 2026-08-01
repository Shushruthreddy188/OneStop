package com.onestop.order.web.dto;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.util.List;

public final class OrderDtos {

    private OrderDtos() {
    }

    public record CheckoutRequest(
            @NotBlank String recipientName,
            String phone,
            @NotBlank String line1,
            String line2,
            @NotBlank String city,
            String state,
            String postalCode,
            @NotBlank String country,
            String paymentMethod,
            String idempotencyKey) {
    }

    public record OrderItemDto(
            Long id,
            Long productId,
            String sku,
            String productName,
            int quantity,
            BigDecimal unitPrice,
            BigDecimal lineTotal) {
    }

    public record OrderAddressDto(
            String recipientName,
            String phone,
            String line1,
            String line2,
            String city,
            String state,
            String postalCode,
            String country) {
    }

    public record OrderDto(
            Long id,
            String status,
            BigDecimal subtotal,
            BigDecimal tax,
            BigDecimal deliveryFee,
            BigDecimal total,
            String paymentMethod,
            List<OrderItemDto> items,
            OrderAddressDto address) {
    }

    public record OrderSummaryDto(
            Long id,
            String status,
            BigDecimal total,
            String paymentMethod,
            int itemCount) {
    }
}
