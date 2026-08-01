package com.onestop.order.client.dto;

import java.math.BigDecimal;
import java.util.List;

/** Payloads exchanged with the cart, catalog, and inventory services. */
public final class ClientDtos {

    private ClientDtos() {
    }

    public record CartView(Long cartId, List<CartLine> items, BigDecimal subtotal, int totalItems) {
    }

    public record CartLine(Long itemId, Long productId, String productName,
                           BigDecimal unitPrice, int quantity, BigDecimal lineTotal) {
    }

    public record CatalogProduct(Long id, String sku, String name,
                                 BigDecimal sellingPrice, String status) {
    }

    public record ReserveLine(Long productId, int quantity) {
    }

    public record ReserveRequest(Long orderId, List<ReserveLine> items) {
    }

    public record ReservationResult(Long reservationId, String status) {
    }
}
