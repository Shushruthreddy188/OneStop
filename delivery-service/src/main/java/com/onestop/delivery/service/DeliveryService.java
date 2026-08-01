package com.onestop.delivery.service;

import com.onestop.delivery.domain.Shipment;
import com.onestop.delivery.domain.ShipmentEvent;
import com.onestop.delivery.repo.ShipmentRepository;
import com.onestop.delivery.web.dto.DeliveryDtos.ShipmentDto;
import com.onestop.delivery.web.dto.DeliveryDtos.ShipmentEventDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class DeliveryService {

    private static final Logger log = LoggerFactory.getLogger(DeliveryService.class);

    /** Ordered shipment lifecycle. */
    public static final List<String> STAGES =
            List.of("CONFIRMED", "PACKED", "SHIPPED", "OUT_FOR_DELIVERY", "DELIVERED");
    private static final String DELIVERED = "DELIVERED";

    private final ShipmentRepository shipments;

    public DeliveryService(ShipmentRepository shipments) {
        this.shipments = shipments;
    }

    /** Create a shipment for a newly confirmed order (idempotent per order). */
    @Transactional
    public void createFromOrder(Long orderId, Long customerId) {
        if (orderId == null || shipments.existsByOrderId(orderId)) {
            return;
        }
        Shipment shipment = new Shipment();
        shipment.setOrderId(orderId);
        shipment.setCustomerId(customerId);
        shipment.setCourier("OneStop Express");
        shipment.setTrackingNumber("OS" + ThreadLocalRandom.current().nextLong(100000000L, 999999999L));
        shipment.addEvent("CONFIRMED", "Order confirmed and ready to be packed");
        shipments.save(shipment);
        log.info("Created shipment for order {}", orderId);
    }

    @Transactional(readOnly = true)
    public Optional<ShipmentDto> getByOrder(Long customerId, Long orderId) {
        return shipments.findByOrderIdAndCustomerId(orderId, customerId).map(DeliveryService::toDto);
    }

    /** Advance every in-transit shipment one stage (called by the simulator). */
    @Transactional
    public int advanceActiveShipments() {
        List<Shipment> active = shipments.findByStatusNot(DELIVERED);
        for (Shipment s : active) {
            int idx = STAGES.indexOf(s.getStatus());
            if (idx >= 0 && idx < STAGES.size() - 1) {
                String next = STAGES.get(idx + 1);
                s.addEvent(next, noteFor(next));
            }
        }
        return active.size();
    }

    private static String noteFor(String stage) {
        return switch (stage) {
            case "PACKED" -> "Packed at the fulfilment centre";
            case "SHIPPED" -> "Shipped — on its way to your city";
            case "OUT_FOR_DELIVERY" -> "Out for delivery";
            case "DELIVERED" -> "Delivered";
            default -> stage;
        };
    }

    private static ShipmentDto toDto(Shipment s) {
        List<ShipmentEventDto> events = s.getEvents().stream()
                .map(e -> new ShipmentEventDto(e.getStatus(), e.getNote(), e.getOccurredAt()))
                .toList();
        return new ShipmentDto(s.getId(), s.getOrderId(), s.getStatus(), s.getCourier(),
                s.getTrackingNumber(), s.getCreatedAt(), events);
    }
}
