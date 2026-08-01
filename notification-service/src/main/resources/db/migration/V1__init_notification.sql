-- Notification Service initial schema.
-- MVP: records simulated notifications. Order success must NOT depend on delivery
-- here; failures are logged and retried later (and moved to Kafka in Milestone 6).

CREATE TABLE notification_log (
    id         BIGSERIAL PRIMARY KEY,
    order_id   BIGINT,
    channel    VARCHAR(30)  NOT NULL DEFAULT 'CONSOLE',
    recipient  VARCHAR(255),
    subject    VARCHAR(255),
    body       TEXT,
    status     VARCHAR(30)  NOT NULL DEFAULT 'SENT',
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_notification_order ON notification_log (order_id);
