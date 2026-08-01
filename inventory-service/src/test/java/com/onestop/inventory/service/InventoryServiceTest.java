package com.onestop.inventory.service;

import com.onestop.inventory.domain.InventoryReservation;
import com.onestop.inventory.domain.InventoryReservationItem;
import com.onestop.inventory.repo.InventoryRepository;
import com.onestop.inventory.repo.ReservationItemRepository;
import com.onestop.inventory.repo.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock InventoryRepository inventory;
    @Mock ReservationRepository reservations;
    @Mock ReservationItemRepository reservationItems;

    private InventoryService service;

    @BeforeEach
    void setUp() {
        service = new InventoryService(inventory, reservations, reservationItems);
    }

    @Test
    void confirmUsesLockedReservationAndMovesReservedStockOnce() {
        InventoryReservation reservation = reservation(12L, InventoryReservation.PENDING);
        when(reservations.findByIdForUpdate(12L)).thenReturn(Optional.of(reservation));
        when(reservationItems.findByReservationId(12L))
                .thenReturn(List.of(new InventoryReservationItem(12L, 42L, 3)));

        var response = service.confirm(12L);

        verify(inventory).confirm(42L, 3);
        assertThat(response.status()).isEqualTo(InventoryReservation.CONFIRMED);
    }

    @Test
    void repeatedConfirmDoesNotDecrementReservedStockAgain() {
        InventoryReservation reservation = reservation(12L, InventoryReservation.CONFIRMED);
        when(reservations.findByIdForUpdate(12L)).thenReturn(Optional.of(reservation));

        var response = service.confirm(12L);

        verify(inventory, never()).confirm(42L, 3);
        assertThat(response.status()).isEqualTo(InventoryReservation.CONFIRMED);
    }

    private static InventoryReservation reservation(Long id, String status) {
        InventoryReservation reservation = new InventoryReservation();
        reservation.setId(id);
        reservation.setStatus(status);
        return reservation;
    }
}
