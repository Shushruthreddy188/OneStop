-- Recommendation signals derived from the activity stream. These tables hold
-- only aggregated, non-PII behavioural counts and per-customer view history.

-- Popularity signal per product: how many PRODUCT_VIEWED events we have seen.
-- Drives "Trending now" and the cold-start fallback for every other surface.
CREATE TABLE product_signal (
    product_id  BIGINT      PRIMARY KEY,
    view_count  BIGINT      NOT NULL DEFAULT 0,
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_product_signal_views ON product_signal (view_count DESC);

-- Per-customer recent views (one row per customer+product, refreshed on re-view).
-- Drives "Recently viewed" and the personalized "For you" surface.
CREATE TABLE customer_recent_view (
    id          BIGSERIAL   PRIMARY KEY,
    customer_id BIGINT      NOT NULL,
    product_id  BIGINT      NOT NULL,
    viewed_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_customer_recent UNIQUE (customer_id, product_id)
);

CREATE INDEX idx_recent_customer ON customer_recent_view (customer_id, viewed_at DESC);

-- De-duplication ledger so the activity consumer is idempotent: an activity
-- event is applied to the signals above at most once, even on Kafka redelivery.
CREATE TABLE processed_event (
    event_id     VARCHAR(120) PRIMARY KEY,
    processed_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);
