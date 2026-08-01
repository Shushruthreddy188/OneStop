-- An order has one payment decision. Repeated requests return this same row.
-- Remove duplicate historical rows deterministically before adding the invariant.
DELETE FROM payment duplicate
USING payment keeper
WHERE duplicate.order_id = keeper.order_id
  AND duplicate.id > keeper.id;

CREATE UNIQUE INDEX uq_payment_order ON payment (order_id);
