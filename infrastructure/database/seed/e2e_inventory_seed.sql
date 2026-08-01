-- Stock corresponding to e2e_catalog_seed.sql.
INSERT INTO inventory (
    product_id, location_id, available_quantity, reserved_quantity, version)
VALUES (1, 1, 100, 0, 0)
ON CONFLICT (product_id, location_id) DO UPDATE
SET available_quantity = 100,
    reserved_quantity = 0,
    version = inventory.version + 1;
