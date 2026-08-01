package com.onestop.inventory.web;

import com.onestop.inventory.service.InventoryService;
import com.onestop.inventory.web.dto.InventoryDtos.AvailabilityDto;
import com.onestop.inventory.web.dto.InventoryDtos.ReservationResponse;
import com.onestop.inventory.web.dto.InventoryDtos.ReserveRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Internal API (not exposed through the gateway). Called service-to-service by
 * the Order Service during checkout.
 */
@RestController
@RequestMapping("/internal/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/products/{productId}")
    public AvailabilityDto availability(@PathVariable Long productId) {
        return inventoryService.getAvailability(productId);
    }

    @PostMapping("/reservations")
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponse reserve(@Valid @RequestBody ReserveRequest request) {
        return inventoryService.reserve(request);
    }

    @PostMapping("/restock")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void restock(@Valid @RequestBody ReserveRequest request) {
        inventoryService.restock(request);
    }

    @PostMapping("/reservations/{reservationId}/confirm")
    public ReservationResponse confirm(@PathVariable Long reservationId) {
        return inventoryService.confirm(reservationId);
    }

    @DeleteMapping("/reservations/{reservationId}")
    public ReservationResponse release(@PathVariable Long reservationId) {
        return inventoryService.release(reservationId);
    }
}
