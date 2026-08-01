-- Checkout idempotency is scoped to a customer. Different customers may use
-- the same client-generated key without conflicting with one another.
ALTER TABLE orders DROP CONSTRAINT IF EXISTS orders_idempotency_key_key;

ALTER TABLE orders
    ADD CONSTRAINT uq_orders_customer_idempotency
        UNIQUE (customer_id, idempotency_key);
