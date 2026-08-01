package com.onestop.order.repo;

import com.onestop.order.domain.Order;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @EntityGraph(attributePaths = "items")
    Optional<Order> findByIdAndCustomerId(Long id, Long customerId);

    List<Order> findByCustomerIdOrderByIdDesc(Long customerId);

    @EntityGraph(attributePaths = "items")
    List<Order> findTop100ByStatusOrderByIdAsc(String status);

    /** Idempotency: a given key yields at most one order per customer. */
    @EntityGraph(attributePaths = "items")
    Optional<Order> findByCustomerIdAndIdempotencyKey(Long customerId, String idempotencyKey);
}
