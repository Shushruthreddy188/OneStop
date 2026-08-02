package com.onestop.recommendation.repo;

import com.onestop.recommendation.domain.CustomerRecentView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CustomerRecentViewRepository extends JpaRepository<CustomerRecentView, Long> {

    /** Upsert the customer's last-viewed timestamp for a product. */
    @Modifying
    @Query(value = """
            INSERT INTO customer_recent_view (customer_id, product_id, viewed_at)
            VALUES (:customerId, :productId, now())
            ON CONFLICT (customer_id, product_id)
            DO UPDATE SET viewed_at = now()
            """, nativeQuery = true)
    void touch(@Param("customerId") Long customerId, @Param("productId") Long productId);

    /** A customer's most-recently viewed products, newest first. */
    @Query(value = """
            SELECT product_id FROM customer_recent_view
            WHERE customer_id = :customerId
            ORDER BY viewed_at DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<Long> recentProductIds(@Param("customerId") Long customerId, @Param("limit") int limit);
}
