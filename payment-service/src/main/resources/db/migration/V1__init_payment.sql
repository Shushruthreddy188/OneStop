-- Payment Service schema. One or more payment attempts per order.

CREATE TABLE payment (
    id             BIGSERIAL PRIMARY KEY,
    order_id       BIGINT      NOT NULL,
    customer_id    BIGINT      NOT NULL,
    amount         NUMERIC(12, 2) NOT NULL,
    currency       VARCHAR(3)  NOT NULL DEFAULT 'INR',
    method         VARCHAR(20) NOT NULL,
    status         VARCHAR(20) NOT NULL,
    provider       VARCHAR(20) NOT NULL,
    provider_ref   VARCHAR(100),
    failure_reason VARCHAR(255),
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_payment_order ON payment (order_id);
CREATE INDEX idx_payment_customer ON payment (customer_id);
