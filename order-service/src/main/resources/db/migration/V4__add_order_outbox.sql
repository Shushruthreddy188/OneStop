CREATE TABLE order_outbox (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id),
    event_type VARCHAR(80) NOT NULL,
    payload TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    attempts INTEGER NOT NULL DEFAULT 0,
    next_attempt_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    published_at TIMESTAMPTZ,
    last_error VARCHAR(1000),
    CONSTRAINT uq_order_outbox_event UNIQUE (order_id, event_type)
);

CREATE INDEX idx_order_outbox_pending
    ON order_outbox (status, next_attempt_at, id);
