-- Search Service index. A denormalized, read-optimized copy of the catalog,
-- kept in sync via reindex (and Kafka product events in a later increment).
-- Trigram index enables fast fuzzy / typo-tolerant autocomplete.

CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE TABLE search_product (
    product_id    BIGINT PRIMARY KEY,
    name          VARCHAR(255) NOT NULL,
    brand_name    VARCHAR(150),
    category_name VARCHAR(150),
    package_size  VARCHAR(100),
    selling_price NUMERIC(12, 2),
    mrp           NUMERIC(12, 2)
);

CREATE INDEX idx_search_product_name_trgm ON search_product USING gin (name gin_trgm_ops);
CREATE INDEX idx_search_product_category ON search_product (category_name);
CREATE INDEX idx_search_product_brand ON search_product (brand_name);
