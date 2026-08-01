package com.onestop.order.service;

import com.onestop.order.domain.OrderOutboxEvent;
import com.onestop.order.repo.OrderOutboxRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class OrderOutboxStateStore {
    private final OrderOutboxRepository outbox;

    public OrderOutboxStateStore(OrderOutboxRepository outbox) {
        this.outbox = outbox;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markPublished(Long id) {
        OrderOutboxEvent event = outbox.findById(id).orElseThrow();
        event.setStatus(OrderOutboxEvent.PUBLISHED);
        event.setPublishedAt(Instant.now());
        event.setLastError(null);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void scheduleRetry(Long id, String error) {
        OrderOutboxEvent event = outbox.findById(id).orElseThrow();
        int attempts = event.getAttempts() + 1;
        event.setAttempts(attempts);
        event.setLastError(error == null ? "Kafka publication failed" : error.substring(0, Math.min(1000, error.length())));
        event.setNextAttemptAt(Instant.now().plusSeconds(Math.min(300, 1L << Math.min(attempts, 8))));
    }
}
