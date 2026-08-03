package com.onestop.recommendation.web.dto;

import java.math.BigDecimal;
import java.util.List;

/** Response shapes for the recommendation surfaces. */
public final class RecommendationDtos {

    private RecommendationDtos() {
    }

    /** One recommended product, enriched for display, with an explanation. */
    public record RecommendedProduct(
            Long productId,
            String name,
            String brandName,
            String imageUrl,
            BigDecimal sellingPrice,
            BigDecimal mrp,
            String reason
    ) {
    }

    /**
     * A named row of recommendations.
     *
     * @param surface    stable id of the surface, e.g. "trending" or "for-you"
     * @param title      human title for the row, e.g. "Trending now"
     * @param coldStart  true when a cold-start fallback was used (new/anon user)
     * @param items      the recommended products
     */
    public record RecommendationRow(
            String surface,
            String title,
            boolean coldStart,
            List<RecommendedProduct> items
    ) {
    }
}
