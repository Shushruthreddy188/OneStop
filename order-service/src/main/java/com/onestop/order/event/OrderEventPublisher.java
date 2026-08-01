package com.onestop.order.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class OrderEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(OrderEventPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String topic;

    public OrderEventPublisher(KafkaTemplate<String, Object> kafkaTemplate,
                               @Value("${onestop.kafka.topics.order-confirmed:order.confirmed}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    /**
     * Publish keyed by order id. Best-effort: a publish failure is logged but does
     * not fail the order (the base flow tolerates lost notifications; an outbox
     * pattern for guaranteed delivery comes in the hardening milestone).
     */
    public boolean publishOrderConfirmed(OrderConfirmedEvent event) {
        try {
            kafkaTemplate.send(topic, String.valueOf(event.orderId()), event).get(5, TimeUnit.SECONDS);
            log.info("Published OrderConfirmedEvent for order {}", event.orderId());
            return true;
        } catch (Exception e) {
            log.warn("Failed to publish OrderConfirmedEvent for order {}: {}",
                    event.orderId(), e.getMessage());
            return false;
        }
    }
}
