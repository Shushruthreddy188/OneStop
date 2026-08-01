-- Persist the remote reservation correlation before confirmation. If checkout
-- is interrupted, a retry or reconciliation worker can safely resume it.
ALTER TABLE orders
    ADD COLUMN inventory_reservation_id BIGINT;

CREATE INDEX idx_orders_inventory_reservation
    ON orders (inventory_reservation_id)
    WHERE inventory_reservation_id IS NOT NULL;
