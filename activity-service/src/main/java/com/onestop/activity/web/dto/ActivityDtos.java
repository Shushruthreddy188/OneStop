package com.onestop.activity.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.time.OffsetDateTime;
import java.util.List;

public final class ActivityDtos {

    private ActivityDtos() {
    }

    /** A single client-emitted activity event. customerId is attached server-side from the JWT. */
    public record EventInput(
            @NotBlank String eventId,
            @NotBlank String eventType,
            String sessionId,
            Long productId,
            String query,
            Integer position,
            String source,
            OffsetDateTime occurredAt) {
    }

    public record RecordEventsRequest(@NotEmpty @Valid List<EventInput> events) {
    }

    public record RecordResult(int received, int recorded) {
    }
}
