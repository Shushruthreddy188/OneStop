package com.onestop.delivery.event;

import java.math.BigDecimal;

/** Mirror of the order-service event (matched by JSON field names). */
public record OrderConfirmedEvent(
        Long orderId,
        Long customerId,
        String recipientEmail,
        int itemCount,
        BigDecimal total,
        String occurredAt
) {
}
