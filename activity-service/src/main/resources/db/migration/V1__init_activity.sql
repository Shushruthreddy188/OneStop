-- Activity Service: a durable, idempotent log of customer behavior events.
-- event_id is client-generated (UUID) so redelivered events are deduplicated.
-- No PII is stored here (customer_id is an id, never email/name).

CREATE TABLE activity_event (
    id             BIGSERIAL PRIMARY KEY,
    event_id       VARCHAR(64)  NOT NULL UNIQUE,
    event_type     VARCHAR(40)  NOT NULL,
    session_id     VARCHAR(64),
    customer_id    BIGINT,
    product_id     BIGINT,
    query          VARCHAR(255),
    position       INTEGER,
    source         VARCHAR(40),
    correlation_id VARCHAR(64),
    schema_version INTEGER      NOT NULL DEFAULT 1,
    occurred_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_activity_type_time ON activity_event (event_type, occurred_at);
CREATE INDEX idx_activity_product ON activity_event (product_id);
CREATE INDEX idx_activity_customer ON activity_event (customer_id);
