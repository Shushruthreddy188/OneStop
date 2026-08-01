package com.onestop.notification.event;

import com.onestop.notification.repo.NotificationLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Consumes order-confirmed events and "sends" a simulated notification. This is
 * fully decoupled from checkout: if this service is down, orders still succeed
 * and events are delivered once it comes back (Kafka retains them).
 */
@Component
public class OrderConfirmedListener {

    private static final Logger log = LoggerFactory.getLogger(OrderConfirmedListener.class);

    private final NotificationLogRepository repository;

    public OrderConfirmedListener(NotificationLogRepository repository) {
        this.repository = repository;
    }

    @KafkaListener(
            topics = "${onestop.kafka.topics.order-confirmed:order.confirmed}",
            groupId = "${spring.kafka.consumer.group-id:notification-service}")
    @Transactional
    public void onOrderConfirmed(OrderConfirmedEvent event) {
        String subject = "Order #" + event.orderId() + " confirmed";
        String body = "Thanks! Your order of " + event.itemCount()
                + " item(s) totalling " + event.total() + " is confirmed.";

        int inserted = repository.insertKafkaConfirmationIfAbsent(
                event.orderId(), event.recipientEmail(), subject, body);
        if (inserted == 1) {
            log.info("[NOTIFICATION] (kafka) order={} to={} subject={}",
                    event.orderId(), event.recipientEmail(), subject);
        } else {
            log.info("Ignored duplicate order confirmation event for order {}", event.orderId());
        }
    }
}
