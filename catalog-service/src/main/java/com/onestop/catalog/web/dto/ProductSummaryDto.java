package com.onestop.catalog.web.dto;

import java.math.BigDecimal;

/** Compact product view for list/grid pages. */
public record ProductSummaryDto(
        Long id,
        String sku,
        String name,
        String brandName,
        String categoryName,
        String packageSize,
        String imageUrl,
        BigDecimal mrp,
        BigDecimal sellingPrice,
        String status
) {
}
