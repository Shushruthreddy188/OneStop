package com.onestop.order.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onestop.order.domain.OrderOutboxEvent;
import com.onestop.order.event.OrderConfirmedEvent;
import com.onestop.order.event.OrderEventPublisher;
import com.onestop.order.repo.OrderOutboxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class OrderOutboxPublisher {
    private static final Logger log = LoggerFactory.getLogger(OrderOutboxPublisher.class);
    private final OrderOutboxRepository outbox;
    private final OrderEventPublisher publisher;
    private final OrderOutboxStateStore stateStore;
    private final ObjectMapper objectMapper;

    public OrderOutboxPublisher(OrderOutboxRepository outbox, OrderEventPublisher publisher,
                                OrderOutboxStateStore stateStore, ObjectMapper objectMapper) {
        this.outbox = outbox;
        this.publisher = publisher;
        this.stateStore = stateStore;
        this.objectMapper = objectMapper;
    }

    @Scheduled(initialDelayString = "${onestop.outbox.initial-delay-ms:2000}",
            fixedDelayString = "${onestop.outbox.fixed-delay-ms:2000}")
    public void publishPending() {
        for (OrderOutboxEvent entry : outbox.findTop100ByStatusAndNextAttemptAtLessThanEqualOrderByIdAsc(
                OrderOutboxEvent.PENDING, Instant.now())) {
            try {
                OrderConfirmedEvent event = objectMapper.readValue(entry.getPayload(), OrderConfirmedEvent.class);
                if (publisher.publishOrderConfirmed(event)) {
                    stateStore.markPublished(entry.getId());
                } else {
                    stateStore.scheduleRetry(entry.getId(), "Kafka publication failed");
                }
            } catch (Exception e) {
                log.warn("Outbox event {} could not be published: {}", entry.getId(), e.getMessage());
                stateStore.scheduleRetry(entry.getId(), e.getMessage());
            }
        }
    }
}
