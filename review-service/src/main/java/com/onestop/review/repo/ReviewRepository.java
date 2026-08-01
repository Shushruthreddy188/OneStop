package com.onestop.review.repo;

import com.onestop.review.domain.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    Page<Review> findByProductIdOrderByIdDesc(Long productId, Pageable pageable);

    Optional<Review> findByProductIdAndCustomerId(Long productId, Long customerId);

    /** Aggregate rating for a product. */
    interface Summary {
        long getCount();
        Double getAverage();
    }

    @Query("SELECT COUNT(r) AS count, AVG(r.rating) AS average FROM Review r WHERE r.productId = :productId")
    Summary summarize(@Param("productId") Long productId);
}
