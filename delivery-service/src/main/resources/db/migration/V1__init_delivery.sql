-- Delivery Service schema: one shipment per order, with a timeline of events.

CREATE TABLE shipment (
    id              BIGSERIAL PRIMARY KEY,
    order_id        BIGINT      NOT NULL UNIQUE,
    customer_id     BIGINT      NOT NULL,
    status          VARCHAR(30) NOT NULL,
    courier         VARCHAR(60),
    tracking_number VARCHAR(40),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE shipment_event (
    id          BIGSERIAL PRIMARY KEY,
    shipment_id BIGINT      NOT NULL REFERENCES shipment (id) ON DELETE CASCADE,
    status      VARCHAR(30) NOT NULL,
    note        VARCHAR(200),
    occurred_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_shipment_customer ON shipment (customer_id);
CREATE INDEX idx_shipment_event_shipment ON shipment_event (shipment_id);
