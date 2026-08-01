package com.onestop.delivery.web;

import com.onestop.delivery.service.DeliveryService;
import com.onestop.delivery.web.dto.DeliveryDtos.ShipmentDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/deliveries")
public class DeliveryController {

    private final DeliveryService deliveryService;

    public DeliveryController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    /** GET /api/deliveries/order/{orderId} — tracking timeline for the caller's order. */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<ShipmentDto> byOrder(@AuthenticationPrincipal Long customerId,
                                               @PathVariable Long orderId) {
        return deliveryService.getByOrder(customerId, orderId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
