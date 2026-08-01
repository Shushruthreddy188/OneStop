package com.onestop.order.service;

import com.onestop.order.client.CartClient;
import com.onestop.order.client.CatalogClient;
import com.onestop.order.client.InventoryClient;
import com.onestop.order.client.InventoryInsufficientStockException;
import com.onestop.order.client.dto.ClientDtos.CartLine;
import com.onestop.order.client.dto.ClientDtos.CartView;
import com.onestop.order.client.dto.ClientDtos.CatalogProduct;
import com.onestop.order.client.dto.ClientDtos.ReservationResult;
import com.onestop.order.client.dto.ClientDtos.ReserveLine;
import com.onestop.order.domain.Order;
import com.onestop.order.domain.OrderAddress;
import com.onestop.order.domain.OrderItem;
import com.onestop.order.error.ApiExceptions.DependencyException;
import com.onestop.order.error.ApiExceptions.EmptyCartException;
import com.onestop.order.error.ApiExceptions.InsufficientStockException;
import com.onestop.order.error.ApiExceptions.NotFoundException;
import com.onestop.order.error.ApiExceptions.OrderStateException;
import com.onestop.order.repo.OrderAddressRepository;
import com.onestop.order.repo.OrderRepository;
import com.onestop.order.security.AuthenticatedUser;
import com.onestop.order.web.OrderMapper;
import com.onestop.order.web.dto.OrderDtos.CheckoutRequest;
import com.onestop.order.web.dto.OrderDtos.OrderDto;
import com.onestop.order.web.dto.OrderDtos.OrderSummaryDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orders;
    private final OrderAddressRepository addresses;
    private final CartClient cartClient;
    private final CatalogClient catalogClient;
    private final InventoryClient inventoryClient;
    private final OrderStateStore stateStore;

    public OrderService(OrderRepository orders, OrderAddressRepository addresses,
                        CartClient cartClient, CatalogClient catalogClient,
                        InventoryClient inventoryClient, OrderStateStore stateStore) {
        this.orders = orders;
        this.addresses = addresses;
        this.cartClient = cartClient;
        this.catalogClient = catalogClient;
        this.inventoryClient = inventoryClient;
        this.stateStore = stateStore;
    }

    /** Place-Order flow (architecture doc, section 6). */
    public OrderDto placeOrder(AuthenticatedUser user, CheckoutRequest req) {
        String idempotencyKey = (req.idempotencyKey() == null || req.idempotencyKey().isBlank())
                ? UUID.randomUUID().toString()
                : req.idempotencyKey().trim();

        // (Idempotency) Return the original order if this key was already used.
        var existing = orders.findByCustomerIdAndIdempotencyKey(user.userId(), idempotencyKey);
        if (existing.isPresent()) {
            Order o = existing.get();
            if (Order.STOCK_RESERVED.equals(o.getStatus()) && o.getInventoryReservationId() != null) {
                // A previous request crossed the inventory boundary but did not
                // finish locally. Both operations are idempotent, so resume safely.
                inventoryClient.confirm(o.getInventoryReservationId());
                stateStore.markConfirmed(o.getId(), user.email());
                o.setStatus(Order.CONFIRMED);
            }
            return OrderMapper.toOrderDto(o, addresses.findById(o.getId()).orElse(null));
        }

        // Load the cart and snapshot each line from the catalog (authoritative sku/name/price).
        CartView cart = cartClient.getCart(user.token());
        if (cart == null || cart.items() == null || cart.items().isEmpty()) {
            throw new EmptyCartException();
        }

        List<OrderItem> items = new ArrayList<>();
        List<ReserveLine> reserveLines = new ArrayList<>();
        Map<Long, String> productIdToSku = new LinkedHashMap<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (CartLine line : cart.items()) {
            CatalogProduct product = catalogClient.findProduct(line.productId())
                    .orElseThrow(() -> new DependencyException("Product missing in catalog: " + line.productId()));

            BigDecimal unitPrice = product.sellingPrice();
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(line.quantity()));

            OrderItem item = new OrderItem();
            item.setProductId(product.id());
            item.setSku(product.sku());
            item.setProductName(product.name());
            item.setQuantity(line.quantity());
            item.setUnitPrice(unitPrice);
            item.setLineTotal(lineTotal);
            items.add(item);

            reserveLines.add(new ReserveLine(product.id(), line.quantity()));
            productIdToSku.put(product.id(), product.sku());
            subtotal = subtotal.add(lineTotal);
        }

        // Persist the pending order before crossing the service boundary. This gives
        // inventory a durable business correlation id and makes the customer-scoped
        // idempotency constraint fail before any stock is reserved.
        String paymentMethod = normalizePayment(req.paymentMethod());
        Order order = new Order();
        order.setCustomerId(user.userId());
        order.setStatus(Order.PENDING);
        order.setSubtotal(subtotal);
        order.setTax(BigDecimal.ZERO);
        order.setDeliveryFee(BigDecimal.ZERO);
        order.setTotal(subtotal);
        order.setPaymentMethod(paymentMethod);
        order.setIdempotencyKey(idempotencyKey);
        items.forEach(order::addItem);
        OrderAddress address = toOrderAddress(null, req);
        try {
            stateStore.createPending(order, address);
        } catch (DataIntegrityViolationException duplicate) {
            // A concurrent request won the customer/key race. Its transaction
            // has committed before PostgreSQL reports our unique violation.
            Order winner = orders.findByCustomerIdAndIdempotencyKey(user.userId(), idempotencyKey)
                    .orElseThrow(() -> duplicate);
            return OrderMapper.toOrderDto(winner, addresses.findById(winner.getId()).orElse(null));
        }

        // Reserve stock atomically. A 409 means nothing was reserved -> reject checkout.
        ReservationResult reservation;
        try {
            reservation = inventoryClient.reserve(order.getId(), reserveLines);
        } catch (InventoryInsufficientStockException e) {
            stateStore.markFailed(order.getId());
            List<String> skus = e.getUnavailableProductIds().stream()
                    .map(id -> productIdToSku.getOrDefault(id, "product-" + id))
                    .toList();
            throw new InsufficientStockException(skus);
        }

        boolean inventoryConfirmed = false;
        try {
            // Record the remote correlation before confirmation. This is the
            // recoverable handoff point for retries and reconciliation.
            stateStore.recordReservation(order.getId(), reservation.reservationId());
            order.setInventoryReservationId(reservation.reservationId());
            order.setStatus(Order.STOCK_RESERVED);

            // Commit the reservation (stock is sold).
            inventoryClient.confirm(reservation.reservationId());
            inventoryConfirmed = true;
            stateStore.markConfirmed(order.getId(), user.email());
            order.setStatus(Order.CONFIRMED);

            // Best-effort side effects: order success must not depend on these.
            cartClient.clearCart(user.token());

            return OrderMapper.toOrderDto(order, address);
        } catch (Exception e) {
            if (!inventoryConfirmed) {
                log.warn("Checkout failed before inventory confirmation for reservation {}; releasing. Cause: {}",
                        reservation.reservationId(), e.getMessage());
                inventoryClient.release(reservation.reservationId());
                stateStore.markFailed(order.getId());
            } else {
                // Leave STOCK_RESERVED + reservation id durable. A retry can
                // idempotently confirm inventory and complete the local state.
                log.error("Inventory reservation {} confirmed but order {} needs reconciliation: {}",
                        reservation.reservationId(), order.getId(), e.getMessage());
            }
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public OrderDto getOrder(Long customerId, Long orderId) {
        Order order = orders.findByIdAndCustomerId(orderId, customerId)
                .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));
        return OrderMapper.toOrderDto(order, addresses.findById(order.getId()).orElse(null));
    }

    @Transactional(readOnly = true)
    public List<OrderSummaryDto> listOrders(Long customerId) {
        return orders.findByCustomerIdOrderByIdDesc(customerId).stream()
                .map(OrderMapper::toSummary)
                .toList();
    }

    @Transactional
    public OrderDto cancelOrder(Long customerId, Long orderId) {
        Order order = orders.findByIdAndCustomerId(orderId, customerId)
                .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));
        if (Order.CANCELLED.equals(order.getStatus())) {
            return OrderMapper.toOrderDto(order, addresses.findById(order.getId()).orElse(null));
        }
        if (!Order.CONFIRMED.equals(order.getStatus())) {
            throw new OrderStateException("Order cannot be cancelled in status " + order.getStatus());
        }

        // Return the sold stock to inventory.
        List<ReserveLine> lines = order.getItems().stream()
                .map(i -> new ReserveLine(i.getProductId(), i.getQuantity()))
                .toList();
        inventoryClient.restock(lines);

        order.setStatus(Order.CANCELLED);
        return OrderMapper.toOrderDto(order, addresses.findById(order.getId()).orElse(null));
    }

    private static String normalizePayment(String method) {
        if (method == null || method.isBlank()) return "COD";
        String m = method.trim().toUpperCase();
        return (m.equals("CARD") || m.equals("COD")) ? m : "COD";
    }

    private static OrderAddress toOrderAddress(Long orderId, CheckoutRequest req) {
        OrderAddress a = new OrderAddress();
        a.setOrderId(orderId);
        a.setRecipientName(req.recipientName());
        a.setPhone(req.phone());
        a.setLine1(req.line1());
        a.setLine2(req.line2());
        a.setCity(req.city());
        a.setState(req.state());
        a.setPostalCode(req.postalCode());
        a.setCountry(req.country());
        return a;
    }
}
