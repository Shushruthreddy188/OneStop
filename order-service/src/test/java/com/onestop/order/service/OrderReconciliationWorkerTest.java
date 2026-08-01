package com.onestop.order.service;

import com.onestop.order.client.InventoryClient;
import com.onestop.order.domain.Order;
import com.onestop.order.repo.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderReconciliationWorkerTest {

    @Mock OrderRepository orders;
    @Mock InventoryClient inventoryClient;
    @Mock OrderStateStore stateStore;

    private OrderReconciliationWorker worker;

    @BeforeEach
    void setUp() {
        worker = new OrderReconciliationWorker(orders, inventoryClient, stateStore);
    }

    @Test
    void confirmsAndCompletesRecoverableOrder() {
        Order order = recoverableOrder(41L);
        when(orders.findTop100ByStatusOrderByIdAsc(Order.STOCK_RESERVED))
                .thenReturn(List.of(order));

        worker.reconcile();

        verify(inventoryClient).confirm(71L);
        verify(stateStore).markConfirmed(41L, null);
    }

    @Test
    void leavesMalformedOrderForInvestigation() {
        Order order = recoverableOrder(41L);
        order.setInventoryReservationId(null);
        when(orders.findTop100ByStatusOrderByIdAsc(Order.STOCK_RESERVED))
                .thenReturn(List.of(order));

        worker.reconcile();

        verify(inventoryClient, never()).confirm(any());
        verify(stateStore, never()).markConfirmed(any(), any());
    }

    private static Order recoverableOrder(Long id) {
        Order order = new Order();
        order.setId(id);
        order.setCustomerId(7L);
        order.setStatus(Order.STOCK_RESERVED);
        order.setInventoryReservationId(71L);
        order.setTotal(new BigDecimal("25.00"));
        return order;
    }
}
