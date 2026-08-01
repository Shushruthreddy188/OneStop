package com.onestop.delivery.scheduler;

import com.onestop.delivery.service.DeliveryService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Demo courier: nudges every in-transit shipment one stage forward on a timer,
 * so the order-tracking timeline visibly progresses. A real service would update
 * status from courier webhooks/scans.
 */
@Component
public class ShipmentSimulator {

    private final DeliveryService deliveryService;

    public ShipmentSimulator(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @Scheduled(fixedDelayString = "${onestop.delivery.simulate-interval-ms:25000}",
            initialDelayString = "${onestop.delivery.simulate-interval-ms:25000}")
    public void tick() {
        deliveryService.advanceActiveShipments();
    }
}
