package com.onestop.catalog.web.dto;

import java.math.BigDecimal;

/** Full product view for the detail page. */
public record ProductDetailDto(
        Long id,
        String sku,
        String name,
        String description,
        Long brandId,
        String brandName,
        Long categoryId,
        String categoryName,
        String packageSize,
        String imageUrl,
        BigDecimal mrp,
        BigDecimal sellingPrice,
        String status
) {
}
