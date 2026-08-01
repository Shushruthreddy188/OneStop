package com.onestop.order.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Shipping address snapshot for an order (PK is the order id). */
@Entity
@Table(name = "order_address")
@Getter
@Setter
@NoArgsConstructor
public class OrderAddress {

    @Id
    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "recipient_name", nullable = false)
    private String recipientName;

    private String phone;

    @Column(nullable = false)
    private String line1;
    private String line2;

    @Column(nullable = false)
    private String city;
    private String state;

    @Column(name = "postal_code")
    private String postalCode;

    @Column(nullable = false)
    private String country;
}
