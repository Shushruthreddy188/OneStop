# Event: `order.confirmed`

Published by **order-service** when an order reaches `CONFIRMED`. Consumed by
**notification-service** (consumer group `notification-service`).

- **Topic:** `order.confirmed`
- **Key:** order id (string) — preserves per-order ordering
- **Value:** JSON (no type headers; consumers map by field name)

## Payload

| Field            | Type    | Notes                                  |
|------------------|---------|----------------------------------------|
| `orderId`        | number  | Order id                               |
| `customerId`     | number  | Customer id                            |
| `recipientEmail` | string  | From the JWT `email` claim (nullable)  |
| `itemCount`      | number  | Number of distinct line items          |
| `total`          | number  | Order total                            |
| `occurredAt`     | string  | ISO-8601 instant                       |

```json
{
  "orderId": 42,
  "customerId": 7,
  "recipientEmail": "customer@example.com",
  "itemCount": 3,
  "total": 234.00,
  "occurredAt": "2026-08-01T02:15:00Z"
}
```

## Delivery semantics

At-least-once. The consumer is idempotent enough for MVP (it only appends a
notification log row). A guaranteed-delivery **outbox** on the producer side is a
Milestone 7 hardening item.
