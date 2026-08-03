package com.onestop.recommendation.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;

/**
 * Mirror of the canonical activity event published by activity-service to the
 * {@code activity.events} topic. Only the fields the recommender needs are kept;
 * unknown fields are ignored so the producer can evolve independently.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ActivityEventMessage(
        String eventId,
        String eventType,
        String sessionId,
        Long customerId,
        Long productId,
        String query,
        Integer position,
        String source,
        String correlationId,
        Integer schemaVersion,
        Instant occurredAt
) {
}
