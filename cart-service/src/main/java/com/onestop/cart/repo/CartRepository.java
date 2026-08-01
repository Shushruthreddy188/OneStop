package com.onestop.cart.repo;

import com.onestop.cart.domain.Cart;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    @EntityGraph(attributePaths = "items")
    Optional<Cart> findByCustomerIdAndStatus(Long customerId, String status);

    @Modifying
    @Query(value = """
            INSERT INTO cart (customer_id, status)
            VALUES (:customerId, 'ACTIVE')
            ON CONFLICT (customer_id) WHERE status = 'ACTIVE' DO NOTHING
            """, nativeQuery = true)
    void createActiveCartIfMissing(@Param("customerId") Long customerId);
}
