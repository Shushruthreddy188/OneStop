package com.onestop.notification.event;

import java.math.BigDecimal;

/** Mirror of the order-service event (matched by JSON field names, not type). */
public record OrderConfirmedEvent(
        Long orderId,
        Long customerId,
        String recipientEmail,
        int itemCount,
        BigDecimal total,
        String occurredAt
) {
}
