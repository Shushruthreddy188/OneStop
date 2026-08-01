-- Wishlist Service schema. One row per (customer, product) — saved for later.

CREATE TABLE wishlist_item (
    id            BIGSERIAL PRIMARY KEY,
    customer_id   BIGINT      NOT NULL,
    product_id    BIGINT      NOT NULL,
    product_name  VARCHAR(255),
    selling_price NUMERIC(12, 2),
    mrp           NUMERIC(12, 2),
    added_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_wishlist_customer_product UNIQUE (customer_id, product_id)
);

CREATE INDEX idx_wishlist_customer ON wishlist_item (customer_id);
