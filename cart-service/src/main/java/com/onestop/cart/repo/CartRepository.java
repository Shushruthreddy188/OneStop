package com.onestop.cart.repo;

import com.onestop.cart.domain.Cart;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    @EntityGraph(attributePaths = "items")
    Optional<Cart> findByCustomerIdAndStatus(Long customerId, String status);
}
