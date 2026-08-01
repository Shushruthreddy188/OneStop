-- Address Service schema. Extracted from Identity per the architecture doc's
-- deferred decision (own the address capability once it grows).

CREATE TABLE address (
    id             BIGSERIAL PRIMARY KEY,
    customer_id    BIGINT       NOT NULL,
    label          VARCHAR(50),
    recipient_name VARCHAR(200),
    phone          VARCHAR(30),
    line1          VARCHAR(255) NOT NULL,
    line2          VARCHAR(255),
    city           VARCHAR(150) NOT NULL,
    state          VARCHAR(150),
    postal_code    VARCHAR(30),
    country        VARCHAR(100) NOT NULL,
    is_default     BOOLEAN      NOT NULL DEFAULT false,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_address_customer ON address (customer_id);
