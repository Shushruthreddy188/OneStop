package com.onestop.cart.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public final class CartDtos {

    private CartDtos() {
    }

    public record CartItemDto(
            Long itemId,
            Long productId,
            String productName,
            BigDecimal unitPrice,
            int quantity,
            BigDecimal lineTotal) {
    }

    public record CartDto(
            Long cartId,
            List<CartItemDto> items,
            BigDecimal subtotal,
            int totalItems) {
    }

    public record AddItemRequest(
            @NotNull Long productId,
            @Min(1) int quantity) {
    }

    public record UpdateItemRequest(
            @Min(1) int quantity) {
    }
}
