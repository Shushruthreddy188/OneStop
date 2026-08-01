package com.onestop.inventory.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "inventory_reservation_item")
@IdClass(ReservationItemId.class)
@Getter
@Setter
@NoArgsConstructor
public class InventoryReservationItem {

    @Id
    @Column(name = "reservation_id")
    private Long reservationId;

    @Id
    @Column(name = "product_id")
    private Long productId;

    @Column(nullable = false)
    private int quantity;

    public InventoryReservationItem(Long reservationId, Long productId, int quantity) {
        this.reservationId = reservationId;
        this.productId = productId;
        this.quantity = quantity;
    }
}
