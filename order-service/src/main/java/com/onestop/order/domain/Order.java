package com.onestop.order.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
public class Order {

    public static final String PENDING = "PENDING";
    public static final String STOCK_RESERVED = "STOCK_RESERVED";
    public static final String FAILED = "FAILED";
    public static final String CONFIRMED = "CONFIRMED";
    public static final String CANCELLED = "CANCELLED";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(nullable = false)
    private String status;

    private BigDecimal subtotal;
    private BigDecimal tax;

    @Column(name = "delivery_fee")
    private BigDecimal deliveryFee;

    private BigDecimal discount = BigDecimal.ZERO;

    @Column(name = "coupon_code")
    private String couponCode;

    private BigDecimal total;

    @Column(name = "payment_method")
    private String paymentMethod;

    @Column(name = "idempotency_key")
    private String idempotencyKey;

    @Column(name = "inventory_reservation_id")
    private Long inventoryReservationId;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    public void addItem(OrderItem item) {
        item.setOrder(this);
        items.add(item);
    }
}
