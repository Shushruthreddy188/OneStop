-- Minimal deterministic catalog fixture for CI browser tests.
INSERT INTO brand (id, name)
VALUES (1, 'OneStop Test Brand')
ON CONFLICT (id) DO NOTHING;

INSERT INTO category (id, name, parent_category_id)
VALUES (1, 'E2E Essentials', NULL)
ON CONFLICT (id) DO NOTHING;

INSERT INTO product (
    id, sku, name, description, brand_id, category_id,
    package_size, mrp, selling_price, status)
VALUES (
    1, 'E2E-SKU-001', 'E2E Test Product',
    'Deterministic product used by the automated customer journey.',
    1, 1, '1 unit', 12.50, 10.00, 'ACTIVE')
ON CONFLICT (id) DO NOTHING;
