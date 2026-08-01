package com.onestop.inventory.web;

import com.onestop.inventory.service.InventoryService;
import com.onestop.inventory.web.dto.InventoryDtos.AvailabilityDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public, read-only availability lookup, routed through the gateway so the
 * storefront can show stock. Reservation mutations stay on the /internal API,
 * which the gateway does not expose.
 */
@RestController
@RequestMapping("/api/inventory")
public class PublicInventoryController {

    private final InventoryService inventoryService;

    public PublicInventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/products/{productId}")
    public AvailabilityDto availability(@PathVariable Long productId) {
        return inventoryService.getAvailability(productId);
    }
}
