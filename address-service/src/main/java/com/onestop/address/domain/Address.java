package com.onestop.address.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "address")
@Getter
@Setter
@NoArgsConstructor
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    private String label;

    @Column(name = "recipient_name")
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

    @Column(name = "is_default", nullable = false)
    private boolean isDefault = false;
}
