-- Consolidate any historical duplicate active carts before enforcing the invariant.
WITH ranked AS (
    SELECT id,
           first_value(id) OVER (PARTITION BY customer_id ORDER BY id) AS keep_id
    FROM cart
    WHERE status = 'ACTIVE'
)
UPDATE cart_item item
SET cart_id = ranked.keep_id
FROM ranked
WHERE item.cart_id = ranked.id
  AND ranked.id <> ranked.keep_id;

DELETE FROM cart duplicate
USING cart keeper
WHERE duplicate.status = 'ACTIVE'
  AND keeper.status = 'ACTIVE'
  AND duplicate.customer_id = keeper.customer_id
  AND duplicate.id > keeper.id;

-- Moving items can create repeated product rows. Merge them deterministically.
WITH totals AS (
    SELECT cart_id, product_id, min(id) AS keep_id, sum(quantity) AS total_quantity
    FROM cart_item
    GROUP BY cart_id, product_id
), updated AS (
    UPDATE cart_item item
    SET quantity = totals.total_quantity
    FROM totals
    WHERE item.id = totals.keep_id
    RETURNING item.id
)
DELETE FROM cart_item item
USING totals
WHERE item.cart_id = totals.cart_id
  AND item.product_id = totals.product_id
  AND item.id <> totals.keep_id;

CREATE UNIQUE INDEX uq_cart_active_customer
    ON cart (customer_id)
    WHERE status = 'ACTIVE';

CREATE UNIQUE INDEX uq_cart_item_product
    ON cart_item (cart_id, product_id);
