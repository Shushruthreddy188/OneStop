package com.onestop.recommendation.repo;

import com.onestop.recommendation.domain.ProductSignal;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductSignalRepository extends JpaRepository<ProductSignal, Long> {

    /** Atomic upsert-and-increment of a product's view counter. */
    @Modifying
    @Query(value = """
            INSERT INTO product_signal (product_id, view_count, updated_at)
            VALUES (:productId, 1, now())
            ON CONFLICT (product_id)
            DO UPDATE SET view_count = product_signal.view_count + 1, updated_at = now()
            """, nativeQuery = true)
    void incrementView(@Param("productId") Long productId);

    /** Most-viewed products first — the basis of trending and cold-start. */
    List<ProductSignal> findAllByOrderByViewCountDescProductIdAsc(Limit limit);
}
