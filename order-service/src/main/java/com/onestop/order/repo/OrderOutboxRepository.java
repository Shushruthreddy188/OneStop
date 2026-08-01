package com.onestop.order.repo;

import com.onestop.order.domain.OrderOutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface OrderOutboxRepository extends JpaRepository<OrderOutboxEvent, Long> {
    boolean existsByOrderIdAndEventType(Long orderId, String eventType);
    List<OrderOutboxEvent> findTop100ByStatusAndNextAttemptAtLessThanEqualOrderByIdAsc(
            String status, Instant now);
}
