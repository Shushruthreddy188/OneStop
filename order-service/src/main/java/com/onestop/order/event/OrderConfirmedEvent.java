package com.onestop.order.event;

import java.math.BigDecimal;

/**
 * Published to Kafka when an order is confirmed. Consumers (e.g. notification)
 * react independently, so order success no longer depends on their delivery.
 */
public record OrderConfirmedEvent(
        Long orderId,
        Long customerId,
        String recipientEmail,
        int itemCount,
        BigDecimal total,
        String occurredAt
) {
}
