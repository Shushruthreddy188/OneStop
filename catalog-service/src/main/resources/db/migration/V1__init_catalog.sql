-- Catalog Service initial schema.
-- Catalog answers "What is the product?" (not "how many can we sell?" - that is Inventory).

CREATE TABLE brand (
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL UNIQUE
);

CREATE TABLE category (
    id                 BIGSERIAL PRIMARY KEY,
    name               VARCHAR(150) NOT NULL,
    parent_category_id BIGINT REFERENCES category (id)
);

CREATE TABLE product (
    id            BIGSERIAL PRIMARY KEY,
    sku           VARCHAR(64)  NOT NULL UNIQUE,
    name          VARCHAR(255) NOT NULL,
    description   TEXT,
    brand_id      BIGINT REFERENCES brand (id),
    category_id   BIGINT REFERENCES category (id),
    package_size  VARCHAR(100),
    image_url     VARCHAR(1000),
    mrp           NUMERIC(12, 2),
    selling_price NUMERIC(12, 2) NOT NULL,
    status        VARCHAR(30)  NOT NULL DEFAULT 'ACTIVE',
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_product_category ON product (category_id);
CREATE INDEX idx_product_brand ON product (brand_id);
