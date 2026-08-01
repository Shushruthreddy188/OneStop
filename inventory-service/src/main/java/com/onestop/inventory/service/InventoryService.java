package com.onestop.inventory.service;

import com.onestop.inventory.domain.Inventory;
import com.onestop.inventory.domain.InventoryReservation;
import com.onestop.inventory.domain.InventoryReservationItem;
import com.onestop.inventory.error.ApiExceptions.InsufficientStockException;
import com.onestop.inventory.error.ApiExceptions.NotFoundException;
import com.onestop.inventory.error.ApiExceptions.ReservationStateException;
import com.onestop.inventory.repo.InventoryRepository;
import com.onestop.inventory.repo.ReservationItemRepository;
import com.onestop.inventory.repo.ReservationRepository;
import com.onestop.inventory.web.dto.InventoryDtos.AvailabilityDto;
import com.onestop.inventory.web.dto.InventoryDtos.ReservationLine;
import com.onestop.inventory.web.dto.InventoryDtos.ReservationResponse;
import com.onestop.inventory.web.dto.InventoryDtos.ReserveRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class InventoryService {

    private static final int RESERVATION_TTL_MINUTES = 15;

    private final InventoryRepository inventory;
    private final ReservationRepository reservations;
    private final ReservationItemRepository reservationItems;

    public InventoryService(InventoryRepository inventory,
                            ReservationRepository reservations,
                            ReservationItemRepository reservationItems) {
        this.inventory = inventory;
        this.reservations = reservations;
        this.reservationItems = reservationItems;
    }

    @Transactional(readOnly = true)
    public AvailabilityDto getAvailability(Long productId) {
        Inventory inv = inventory.findByProductId(productId)
                .orElseThrow(() -> new NotFoundException("No inventory for product " + productId));
        return new AvailabilityDto(inv.getProductId(), inv.getAvailableQuantity(), inv.getReservedQuantity());
    }

    /**
     * Reserve all requested quantities atomically. Either every line is reserved
     * or none is: a shortfall on any line throws, rolling back the whole
     * transaction (including any lines already decremented).
     */
    @Transactional
    public ReservationResponse reserve(ReserveRequest req) {
        List<Long> unavailable = new ArrayList<>();
        for (ReservationLine line : req.items()) {
            int updated = inventory.reserve(line.productId(), line.quantity());
            if (updated == 0) {
                unavailable.add(line.productId());
            }
        }
        if (!unavailable.isEmpty()) {
            throw new InsufficientStockException(unavailable);
        }

        InventoryReservation reservation = new InventoryReservation();
        reservation.setOrderId(req.orderId());
        reservation.setStatus(InventoryReservation.PENDING);
        reservation.setExpiresAt(OffsetDateTime.now().plusMinutes(RESERVATION_TTL_MINUTES));
        reservations.save(reservation);

        for (ReservationLine line : req.items()) {
            reservationItems.save(new InventoryReservationItem(
                    reservation.getId(), line.productId(), line.quantity()));
        }

        return toResponse(reservation);
    }

    /** Return committed stock to available (order cancellation). */
    @Transactional
    public void restock(ReserveRequest req) {
        for (ReservationLine line : req.items()) {
            inventory.restock(line.productId(), line.quantity());
        }
    }

    @Transactional
    public ReservationResponse confirm(Long reservationId) {
        InventoryReservation reservation = loadReservation(reservationId);
        if (InventoryReservation.CONFIRMED.equals(reservation.getStatus())) {
            return toResponse(reservation); // idempotent
        }
        if (InventoryReservation.RELEASED.equals(reservation.getStatus())) {
            throw new ReservationStateException("Reservation already released: " + reservationId);
        }
        for (InventoryReservationItem item : reservationItems.findByReservationId(reservationId)) {
            inventory.confirm(item.getProductId(), item.getQuantity());
        }
        reservation.setStatus(InventoryReservation.CONFIRMED);
        return toResponse(reservation);
    }

    @Transactional
    public ReservationResponse release(Long reservationId) {
        InventoryReservation reservation = loadReservation(reservationId);
        if (InventoryReservation.RELEASED.equals(reservation.getStatus())) {
            return toResponse(reservation); // idempotent
        }
        if (InventoryReservation.CONFIRMED.equals(reservation.getStatus())) {
            throw new ReservationStateException("Cannot release a confirmed reservation: " + reservationId);
        }
        for (InventoryReservationItem item : reservationItems.findByReservationId(reservationId)) {
            inventory.release(item.getProductId(), item.getQuantity());
        }
        reservation.setStatus(InventoryReservation.RELEASED);
        return toResponse(reservation);
    }

    private InventoryReservation loadReservation(Long reservationId) {
        // Serialize confirm/release transitions so retries or concurrent callers
        // cannot decrement reserved stock more than once.
        return reservations.findByIdForUpdate(reservationId)
                .orElseThrow(() -> new NotFoundException("Reservation not found: " + reservationId));
    }

    private ReservationResponse toResponse(InventoryReservation r) {
        return new ReservationResponse(r.getId(), r.getStatus(), r.getExpiresAt());
    }
}
