package com.onestop.order.service;

import com.onestop.order.domain.Order;
import com.onestop.order.domain.OrderAddress;
import com.onestop.order.domain.OrderOutboxEvent;
import com.onestop.order.error.ApiExceptions.NotFoundException;
import com.onestop.order.event.OrderConfirmedEvent;
import com.onestop.order.repo.OrderAddressRepository;
import com.onestop.order.repo.OrderOutboxRepository;
import com.onestop.order.repo.OrderRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/** Durable local state transitions around remote inventory operations. */
@Service
public class OrderStateStore {

    private final OrderRepository orders;
    private final OrderAddressRepository addresses;
    private final OrderOutboxRepository outbox;
    private final ObjectMapper objectMapper;

    public OrderStateStore(OrderRepository orders, OrderAddressRepository addresses,
                           OrderOutboxRepository outbox, ObjectMapper objectMapper) {
        this.orders = orders;
        this.addresses = addresses;
        this.outbox = outbox;
        this.objectMapper = objectMapper;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Order createPending(Order order, OrderAddress address) {
        orders.saveAndFlush(order);
        address.setOrderId(order.getId());
        addresses.save(address);
        return order;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordReservation(Long orderId, Long reservationId) {
        Order order = load(orderId);
        order.setInventoryReservationId(reservationId);
        order.setStatus(Order.STOCK_RESERVED);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markConfirmed(Long orderId, String recipientEmail) {
        Order order = load(orderId);
        order.setStatus(Order.CONFIRMED);
        if (!outbox.existsByOrderIdAndEventType(orderId, "ORDER_CONFIRMED")) {
            OrderConfirmedEvent event = new OrderConfirmedEvent(order.getId(), order.getCustomerId(),
                    recipientEmail, order.getItems().size(), order.getTotal(), Instant.now().toString());
            OrderOutboxEvent entry = new OrderOutboxEvent();
            entry.setOrderId(orderId);
            entry.setEventType("ORDER_CONFIRMED");
            try {
                entry.setPayload(objectMapper.writeValueAsString(event));
            } catch (JsonProcessingException e) {
                throw new IllegalStateException("Could not serialize order confirmation event", e);
            }
            outbox.save(entry);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(Long orderId) {
        load(orderId).setStatus(Order.FAILED);
    }

    private Order load(Long orderId) {
        return orders.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));
    }
}
