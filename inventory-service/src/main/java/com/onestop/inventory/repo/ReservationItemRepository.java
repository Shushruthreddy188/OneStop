package com.onestop.inventory.repo;

import com.onestop.inventory.domain.InventoryReservationItem;
import com.onestop.inventory.domain.ReservationItemId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservationItemRepository
        extends JpaRepository<InventoryReservationItem, ReservationItemId> {

    List<InventoryReservationItem> findByReservationId(Long reservationId);
}
