package com.onestop.search.es;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;

/**
 * The Elasticsearch document shape, matching es/product-index.json.
 *
 * <p>{@code popularity} maps to a {@code rank_feature} used for behavioural
 * ranking in Step 4; it is left null (and omitted) until aggregated signals
 * are wired in, because rank_feature values must be strictly positive.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProductDoc(
        Long productId,
        String name,
        String brandName,
        String categoryName,
        String packageSize,
        BigDecimal sellingPrice,
        BigDecimal mrp,
        Float popularity
) {
}
