package com.onestop.activity.repo;

import com.onestop.activity.domain.ActivityEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;

public interface ActivityEventRepository extends JpaRepository<ActivityEvent, Long> {

    boolean existsByEventId(String eventId);

    /** Most-viewed products since a cut-off — feeds "trending" recommendations. */
    @Query("""
            SELECT a.productId AS productId, COUNT(a) AS views
            FROM ActivityEvent a
            WHERE a.eventType = 'PRODUCT_VIEWED' AND a.productId IS NOT NULL AND a.occurredAt >= :since
            GROUP BY a.productId
            ORDER BY COUNT(a) DESC
            """)
    List<ProductCount> topViewedSince(@Param("since") OffsetDateTime since);

    /** Products a customer recently viewed (most recent first, distinct). */
    @Query("""
            SELECT a.productId
            FROM ActivityEvent a
            WHERE a.customerId = :customerId AND a.eventType = 'PRODUCT_VIEWED' AND a.productId IS NOT NULL
            GROUP BY a.productId
            ORDER BY MAX(a.occurredAt) DESC
            """)
    List<Long> recentlyViewedProductIds(@Param("customerId") Long customerId);

    interface ProductCount {
        Long getProductId();
        long getViews();
    }
}
