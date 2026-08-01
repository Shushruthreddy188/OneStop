-- Inventory Service initial schema.
-- Answers "how many can we sell right now?" and owns atomic stock reservation.
-- Identifies products by product_id/SKU without querying the Catalog database.

CREATE TABLE inventory (
    id                 BIGSERIAL PRIMARY KEY,
    product_id         BIGINT      NOT NULL,
    location_id        BIGINT      NOT NULL DEFAULT 1,
    available_quantity INTEGER     NOT NULL DEFAULT 0 CHECK (available_quantity >= 0),
    reserved_quantity  INTEGER     NOT NULL DEFAULT 0 CHECK (reserved_quantity >= 0),
    version            BIGINT      NOT NULL DEFAULT 0,  -- optimistic locking
    updated_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_inventory_product_location UNIQUE (product_id, location_id)
);

CREATE TABLE inventory_reservation (
    id         BIGSERIAL PRIMARY KEY,
    order_id   BIGINT,
    status     VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    expires_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE inventory_reservation_item (
    reservation_id BIGINT NOT NULL REFERENCES inventory_reservation (id) ON DELETE CASCADE,
    product_id     BIGINT NOT NULL,
    quantity       INTEGER NOT NULL CHECK (quantity > 0),
    PRIMARY KEY (reservation_id, product_id)
);

CREATE INDEX idx_inventory_product ON inventory (product_id);
CREATE INDEX idx_reservation_order ON inventory_reservation (order_id);
