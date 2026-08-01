package com.onestop.inventory.domain;

import java.io.Serializable;
import java.util.Objects;

/** Composite key for {@link InventoryReservationItem}. */
public class ReservationItemId implements Serializable {

    private Long reservationId;
    private Long productId;

    public ReservationItemId() {
    }

    public ReservationItemId(Long reservationId, Long productId) {
        this.reservationId = reservationId;
        this.productId = productId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReservationItemId that)) return false;
        return Objects.equals(reservationId, that.reservationId)
                && Objects.equals(productId, that.productId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reservationId, productId);
    }
}
