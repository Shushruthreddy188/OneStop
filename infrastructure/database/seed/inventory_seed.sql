-- Seed stock for the DMart catalog (Milestone 4).
-- Inventory identifies products by productId/SKU without touching the catalog DB,
-- so we seed one row per known product id at the default location (1).
-- Idempotent: existing rows are left untouched.
--
-- Adjust the upper bound to match MAX(product.id) in catalog_db (currently 5187).

INSERT INTO inventory (product_id, location_id, available_quantity, reserved_quantity, version)
SELECT gs, 1, 100, 0, 0
FROM generate_series(1, 5187) AS gs
ON CONFLICT (product_id, location_id) DO NOTHING;
