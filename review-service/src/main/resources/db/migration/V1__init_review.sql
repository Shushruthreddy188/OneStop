-- Review Service schema. One review per customer per product.

CREATE TABLE review (
    id          BIGSERIAL PRIMARY KEY,
    product_id  BIGINT      NOT NULL,
    customer_id BIGINT      NOT NULL,
    rating      SMALLINT    NOT NULL CHECK (rating BETWEEN 1 AND 5),
    title       VARCHAR(150),
    body        TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_review_product_customer UNIQUE (product_id, customer_id)
);

CREATE INDEX idx_review_product ON review (product_id);
