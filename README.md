# OneStop

OneStop is a working retail MVP built as a Java/Spring microservice system with a React frontend.
The current architecture is documented in `OneStop_MVP_Architecture_v0.2.docx`.

## MVP status

The end-to-end customer journey is implemented:

- Register/login with JWT authentication
- Browse and search the product catalog
- Add, update, and remove cart items
- Check inventory availability
- Place an idempotent order with an address and COD/card selection
- Atomically reserve and confirm stock
- View order history and cancel confirmed orders with stock restoration
- Publish order confirmations to Kafka through a transactional outbox
- Consume confirmation events idempotently in the notification service

Checkout is hardened around the cross-service inventory boundary. Orders persist as `PENDING`, then
`STOCK_RESERVED`, and finally `CONFIRMED`. Interrupted confirmations are repaired by a reconciliation
worker. The `CONFIRMED` transition and outbox event are committed together; Kafka publication retries
with exponential backoff without failing checkout. A database-enforced consumer key ensures duplicate
Kafka delivery still creates only one order-confirmation notification.

## Technology

- Frontend: React, TypeScript, Vite, React Router, TanStack Query, React Hook Form
- Backend: Java 21, Spring Boot 3.3.5, Spring Data JPA, Flyway
- Data: PostgreSQL database per service
- Messaging: Kafka
- Edge: Spring Cloud Gateway
- Runtime: Docker Compose
- Tests: JUnit, Mockito, Testcontainers

## Repository layout

```text
frontend/              React customer application
api-gateway/           External entry point (8080)
identity-service/      Authentication, profile, addresses (8081)
catalog-service/       Products, categories, brands (8082)
cart-service/          Customer carts (8083)
inventory-service/     Stock and reservations (8084)
order-service/         Checkout, orders, reconciliation, outbox (8085)
notification-service/  Kafka consumer and notification log (8086)
contracts/             OpenAPI and event contracts
infrastructure/        Docker, database, Kafka, observability assets
docker-compose.yml
pom.xml
```

## Run the MVP

Prerequisites: Docker Desktop. For local builds, use JDK 21 and Node.js 20+.

```bash
docker compose up --build
```

Open the frontend at <http://localhost:5173>. The API gateway is at <http://localhost:8080>.
Each service exposes `/actuator/health` and `/swagger-ui.html` on its own port.

## Verify

```bash
./mvnw test
cd frontend
npm ci
npm run build
```

The backend suite includes a PostgreSQL Testcontainers concurrency test for customer-scoped checkout
idempotency. A full Docker smoke test has also verified order confirmation, stock decrement, duplicate
idempotency behavior, Kafka delivery, and notification persistence.

GitHub Actions runs `clean verify` for every backend module and performs a locked frontend install,
lint, and production build for pull requests and changes to the primary branch.

## Next milestone

The MVP feature path, durable notification pipeline, and CI release gate are complete. The remaining
release hardening is a repeatable end-to-end test, distributed tracing and alerts, dependency/security
remediation, contract tests, secrets management, and deployment to a shared environment.
