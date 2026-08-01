package com.onestop.order.service;

import com.onestop.order.client.CartClient;
import com.onestop.order.client.CatalogClient;
import com.onestop.order.client.InventoryClient;
import com.onestop.order.client.dto.ClientDtos.CartLine;
import com.onestop.order.client.dto.ClientDtos.CartView;
import com.onestop.order.client.dto.ClientDtos.CatalogProduct;
import com.onestop.order.client.dto.ClientDtos.ReservationResult;
import com.onestop.order.domain.Order;
import com.onestop.order.repo.OrderAddressRepository;
import com.onestop.order.repo.OrderRepository;
import com.onestop.order.security.AuthenticatedUser;
import com.onestop.order.web.dto.OrderDtos.CheckoutRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock OrderRepository orders;
    @Mock OrderAddressRepository addresses;
    @Mock CartClient cartClient;
    @Mock CatalogClient catalogClient;
    @Mock InventoryClient inventoryClient;
    @Mock OrderStateStore stateStore;

    private OrderService service;

    @BeforeEach
    void setUp() {
        service = new OrderService(orders, addresses, cartClient, catalogClient,
                inventoryClient, stateStore);
    }

    @Test
    void correlatesInventoryReservationWithPendingOrderId() {
        var user = new AuthenticatedUser(7L, "customer@example.com", "jwt");
        var request = checkout("checkout-123");
        when(orders.findByCustomerIdAndIdempotencyKey(7L, "checkout-123"))
                .thenReturn(Optional.empty());
        when(cartClient.getCart("jwt")).thenReturn(new CartView(3L,
                List.of(new CartLine(4L, 42L, "Rice", new BigDecimal("12.50"),
                        2, new BigDecimal("25.00"))), new BigDecimal("25.00"), 2));
        when(catalogClient.findProduct(42L)).thenReturn(Optional.of(
                new CatalogProduct(42L, "SKU-42", "Rice", new BigDecimal("12.50"), "ACTIVE")));
        when(stateStore.createPending(any(Order.class), any())).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(99L);
            order.getItems().getFirst().setId(100L);
            return order;
        });
        when(inventoryClient.reserve(eq(99L), any())).thenReturn(new ReservationResult(55L, "PENDING"));

        var result = service.placeOrder(user, request);

        ArgumentCaptor<Order> saved = ArgumentCaptor.forClass(Order.class);
        verify(stateStore).createPending(saved.capture(), any());
        verify(stateStore).recordReservation(99L, 55L);
        verify(stateStore).markConfirmed(99L, "customer@example.com");
        verify(inventoryClient).reserve(eq(99L), any());
        verify(inventoryClient).confirm(55L);
        assertThat(saved.getValue().getIdempotencyKey()).isEqualTo("checkout-123");
        assertThat(result.id()).isEqualTo(99L);
        assertThat(result.status()).isEqualTo(Order.CONFIRMED);
    }

    @Test
    void duplicateCheckoutReturnsExistingOrderWithoutCallingDependencies() {
        Order existing = new Order();
        existing.setId(99L);
        existing.setCustomerId(7L);
        existing.setStatus(Order.CONFIRMED);
        existing.setSubtotal(BigDecimal.TEN);
        existing.setTax(BigDecimal.ZERO);
        existing.setDeliveryFee(BigDecimal.ZERO);
        existing.setTotal(BigDecimal.TEN);
        existing.setPaymentMethod("COD");
        existing.setIdempotencyKey("checkout-123");
        when(orders.findByCustomerIdAndIdempotencyKey(7L, "checkout-123"))
                .thenReturn(Optional.of(existing));

        var result = service.placeOrder(
                new AuthenticatedUser(7L, "customer@example.com", "jwt"),
                checkout("checkout-123"));

        assertThat(result.id()).isEqualTo(99L);
        verify(cartClient, never()).getCart(any());
        verify(inventoryClient, never()).reserve(any(), any());
    }

    @Test
    void concurrentIdempotencyRaceReturnsTheWinningOrder() {
        var user = new AuthenticatedUser(7L, "customer@example.com", "jwt");
        Order winner = new Order();
        winner.setId(101L);
        winner.setCustomerId(7L);
        winner.setStatus(Order.PENDING);
        winner.setSubtotal(BigDecimal.TEN);
        winner.setTax(BigDecimal.ZERO);
        winner.setDeliveryFee(BigDecimal.ZERO);
        winner.setTotal(BigDecimal.TEN);
        winner.setPaymentMethod("COD");
        winner.setIdempotencyKey("race-key");

        when(orders.findByCustomerIdAndIdempotencyKey(7L, "race-key"))
                .thenReturn(Optional.empty(), Optional.of(winner));
        when(cartClient.getCart("jwt")).thenReturn(new CartView(3L,
                List.of(new CartLine(4L, 42L, "Rice", BigDecimal.TEN, 1, BigDecimal.TEN)),
                BigDecimal.TEN, 1));
        when(catalogClient.findProduct(42L)).thenReturn(Optional.of(
                new CatalogProduct(42L, "SKU-42", "Rice", BigDecimal.TEN, "ACTIVE")));
        doThrow(new DataIntegrityViolationException("duplicate"))
                .when(stateStore).createPending(any(Order.class), any());

        var result = service.placeOrder(user, checkout("race-key"));

        assertThat(result.id()).isEqualTo(101L);
        verify(inventoryClient, never()).reserve(any(), any());
    }

    private static CheckoutRequest checkout(String idempotencyKey) {
        return new CheckoutRequest("Customer", "555-0100", "1 Main St", null,
                "Chicago", "IL", "60601", "US", "COD", idempotencyKey);
    }
}
