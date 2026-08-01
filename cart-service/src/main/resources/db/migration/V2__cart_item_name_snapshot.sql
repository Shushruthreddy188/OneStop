-- Snapshot the product name at add-to-cart time, alongside the price snapshot.
-- Keeps the cart renderable without calling the Catalog Service on every view,
-- and consistent with the "cart captures intent at a point in time" boundary.
ALTER TABLE cart_item ADD COLUMN product_name_snapshot VARCHAR(255);
