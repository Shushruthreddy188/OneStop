package com.onestop.inventory.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.List;

public final class InventoryDtos {

    private InventoryDtos() {
    }

    public record AvailabilityDto(
            Long productId,
            int availableQuantity,
            int reservedQuantity) {
    }

    public record ReservationLine(
            @NotNull Long productId,
            @Min(1) int quantity) {
    }

    public record ReserveRequest(
            Long orderId,
            @NotEmpty List<@Valid ReservationLine> items) {
    }

    public record ReservationResponse(
            Long reservationId,
            String status,
            OffsetDateTime expiresAt) {
    }
}
