-- Cart Service initial schema.
-- A cart is editable intent. Adding to a cart does NOT reserve inventory.

CREATE TABLE cart (
    id          BIGSERIAL PRIMARY KEY,
    customer_id BIGINT      NOT NULL,
    status      VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE cart_item (
    id                  BIGSERIAL PRIMARY KEY,
    cart_id             BIGINT NOT NULL REFERENCES cart (id) ON DELETE CASCADE,
    product_id          BIGINT NOT NULL,
    quantity            INTEGER NOT NULL CHECK (quantity > 0),
    unit_price_snapshot NUMERIC(12, 2) NOT NULL
);

CREATE INDEX idx_cart_customer ON cart (customer_id);
CREATE INDEX idx_cart_item_cart ON cart_item (cart_id);
