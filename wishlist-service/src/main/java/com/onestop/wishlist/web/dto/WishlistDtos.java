package com.onestop.wishlist.web.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public final class WishlistDtos {

    private WishlistDtos() {
    }

    public record AddItemRequest(@NotNull Long productId) {
    }

    public record WishlistItemDto(
            Long id,
            Long productId,
            String productName,
            BigDecimal sellingPrice,
            BigDecimal mrp) {
    }

    public record WishlistDto(List<WishlistItemDto> items, int count) {
    }
}
