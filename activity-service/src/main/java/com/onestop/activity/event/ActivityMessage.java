package com.onestop.activity.event;

import java.time.OffsetDateTime;

/**
 * Canonical activity event published to Kafka (topic activity.events) for
 * downstream consumers (recommendations, analytics). Contains no PII.
 */
public record ActivityMessage(
        String eventId,
        String eventType,
        String sessionId,
        Long customerId,
        Long productId,
        String query,
        Integer position,
        String source,
        String correlationId,
        int schemaVersion,
        OffsetDateTime occurredAt
) {
}
