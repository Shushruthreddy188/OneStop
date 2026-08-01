package com.onestop.delivery.web.dto;

import java.time.OffsetDateTime;
import java.util.List;

public final class DeliveryDtos {

    private DeliveryDtos() {
    }

    public record ShipmentEventDto(String status, String note, OffsetDateTime occurredAt) {
    }

    public record ShipmentDto(
            Long id,
            Long orderId,
            String status,
            String courier,
            String trackingNumber,
            OffsetDateTime createdAt,
            List<ShipmentEventDto> events) {
    }
}
