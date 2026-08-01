package com.onestop.inventory.repo;

import com.onestop.inventory.domain.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findByProductId(Long productId);

    /**
     * Atomically move stock from available to reserved, but only if enough is
     * available. Returns the number of rows updated: 1 = reserved, 0 = not enough
     * stock. The {@code WHERE available_quantity >= :qty} guard combined with the
     * row lock held during UPDATE makes this safe under concurrency — two callers
     * racing for the last unit cannot both succeed, and stock can never go negative.
     */
    @Modifying
    @Query("""
            UPDATE Inventory i
               SET i.availableQuantity = i.availableQuantity - :qty,
                   i.reservedQuantity  = i.reservedQuantity + :qty,
                   i.version = i.version + 1
             WHERE i.productId = :productId
               AND i.availableQuantity >= :qty
            """)
    int reserve(@Param("productId") Long productId, @Param("qty") int qty);

    /** Release a hold: return quantity to available and drop it from reserved. */
    @Modifying
    @Query("""
            UPDATE Inventory i
               SET i.availableQuantity = i.availableQuantity + :qty,
                   i.reservedQuantity  = i.reservedQuantity - :qty,
                   i.version = i.version + 1
             WHERE i.productId = :productId
            """)
    int release(@Param("productId") Long productId, @Param("qty") int qty);

    /** Return committed stock to available (e.g. when an order is cancelled). */
    @Modifying
    @Query("""
            UPDATE Inventory i
               SET i.availableQuantity = i.availableQuantity + :qty,
                   i.version = i.version + 1
             WHERE i.productId = :productId
            """)
    int restock(@Param("productId") Long productId, @Param("qty") int qty);

    /** Confirm a hold (stock is sold): just drop it from reserved. */
    @Modifying
    @Query("""
            UPDATE Inventory i
               SET i.reservedQuantity = i.reservedQuantity - :qty,
                   i.version = i.version + 1
             WHERE i.productId = :productId
            """)
    int confirm(@Param("productId") Long productId, @Param("qty") int qty);
}
