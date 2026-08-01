-- Retain the earliest Kafka notification if historical duplicate deliveries exist.
DELETE FROM notification_log newer
USING notification_log older
WHERE newer.channel = 'KAFKA'
  AND older.channel = 'KAFKA'
  AND newer.order_id = older.order_id
  AND newer.id > older.id;

-- Kafka is at-least-once. One confirmation notification per order is the MVP
-- consumer idempotency key; console/API test notifications remain unrestricted.
CREATE UNIQUE INDEX uq_notification_kafka_order
    ON notification_log (order_id)
    WHERE channel = 'KAFKA' AND order_id IS NOT NULL;
