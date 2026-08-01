package com.onestop.cart.client;

import java.math.BigDecimal;

/**
 * Subset of the Catalog Service product response that the cart needs to snapshot.
 * Unknown JSON fields are ignored (Spring Boot disables FAIL_ON_UNKNOWN_PROPERTIES).
 */
public record ProductInfo(
        Long id,
        String name,
        BigDecimal sellingPrice,
        String status
) {
}
