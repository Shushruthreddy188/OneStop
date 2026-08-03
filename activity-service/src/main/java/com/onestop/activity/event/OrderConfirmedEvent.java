package com.onestop.activity.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

/** Mirror of the order-service event (matched by field name). */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderConfirmedEvent(
        Long orderId,
        Long customerId,
        String recipientEmail,
        int itemCount,
        BigDecimal total,
        String occurredAt
) {
}
