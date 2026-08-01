package com.onestop.order.service;

import com.onestop.order.client.InventoryClient;
import com.onestop.order.domain.Order;
import com.onestop.order.repo.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


/** Repairs checkouts interrupted after a durable inventory reservation. */
@Component
@ConditionalOnProperty(name = "onestop.reconciliation.enabled", havingValue = "true", matchIfMissing = true)
public class OrderReconciliationWorker {

    private static final Logger log = LoggerFactory.getLogger(OrderReconciliationWorker.class);

    private final OrderRepository orders;
    private final InventoryClient inventoryClient;
    private final OrderStateStore stateStore;

    public OrderReconciliationWorker(OrderRepository orders, InventoryClient inventoryClient,
                                     OrderStateStore stateStore) {
        this.orders = orders;
        this.inventoryClient = inventoryClient;
        this.stateStore = stateStore;
    }

    @Scheduled(
            initialDelayString = "${onestop.reconciliation.initial-delay-ms:30000}",
            fixedDelayString = "${onestop.reconciliation.fixed-delay-ms:30000}")
    public void reconcile() {
        for (Order order : orders.findTop100ByStatusOrderByIdAsc(Order.STOCK_RESERVED)) {
            reconcile(order);
        }
    }

    void reconcile(Order order) {
        Long reservationId = order.getInventoryReservationId();
        if (reservationId == null) {
            log.error("Order {} is STOCK_RESERVED without an inventory reservation id", order.getId());
            return;
        }

        try {
            // Inventory confirmation is idempotent, making repeated worker runs safe.
            inventoryClient.confirm(reservationId);
            stateStore.markConfirmed(order.getId(), null);
            log.info("Reconciled order {} using inventory reservation {}", order.getId(), reservationId);
        } catch (Exception e) {
            log.warn("Could not reconcile order {} with reservation {}: {}",
                    order.getId(), reservationId, e.getMessage());
        }
    }
}
