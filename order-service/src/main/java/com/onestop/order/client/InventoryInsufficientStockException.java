package com.onestop.order.client;

import java.util.List;

/** Thrown by {@link InventoryClient} when a reservation is rejected (HTTP 409). */
public class InventoryInsufficientStockException extends RuntimeException {

    private final transient List<Long> unavailableProductIds;

    public InventoryInsufficientStockException(List<Long> unavailableProductIds) {
        super("Inventory rejected reservation for products: " + unavailableProductIds);
        this.unavailableProductIds = unavailableProductIds;
    }

    public List<Long> getUnavailableProductIds() {
        return unavailableProductIds;
    }
}
