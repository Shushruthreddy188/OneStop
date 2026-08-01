-- Order Service initial schema.
-- An order is an immutable business record: it stores snapshots of product name,
-- SKU, unit price, and quantity so history stays accurate when catalog/prices change.

CREATE TABLE orders (
    id              BIGSERIAL PRIMARY KEY,
    customer_id     BIGINT      NOT NULL,
    status          VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    subtotal        NUMERIC(12, 2) NOT NULL DEFAULT 0,
    tax             NUMERIC(12, 2) NOT NULL DEFAULT 0,
    delivery_fee    NUMERIC(12, 2) NOT NULL DEFAULT 0,
    total           NUMERIC(12, 2) NOT NULL DEFAULT 0,
    payment_method  VARCHAR(30) NOT NULL DEFAULT 'COD',
    idempotency_key VARCHAR(100) UNIQUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE order_item (
    id           BIGSERIAL PRIMARY KEY,
    order_id     BIGINT NOT NULL REFERENCES orders (id) ON DELETE CASCADE,
    product_id   BIGINT NOT NULL,
    sku          VARCHAR(64)  NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    quantity     INTEGER      NOT NULL CHECK (quantity > 0),
    unit_price   NUMERIC(12, 2) NOT NULL,
    line_total   NUMERIC(12, 2) NOT NULL
);

CREATE TABLE order_address (
    order_id       BIGINT PRIMARY KEY REFERENCES orders (id) ON DELETE CASCADE,
    recipient_name VARCHAR(200) NOT NULL,
    phone          VARCHAR(30),
    line1          VARCHAR(255) NOT NULL,
    line2          VARCHAR(255),
    city           VARCHAR(150) NOT NULL,
    state          VARCHAR(150),
    postal_code    VARCHAR(30),
    country        VARCHAR(100) NOT NULL
);

CREATE INDEX idx_orders_customer ON orders (customer_id);
CREATE INDEX idx_order_item_order ON order_item (order_id);
