# OneStop

OneStop is a working retail platform built as a Java/Spring microservice system with a React frontend.
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
- Messaging and cache: Kafka, Redis
- Observability: Prometheus, Grafana, Zipkin
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
search-service/        Typo-tolerant search and autocomplete (8087)
review-service/        Product ratings and reviews (8088)
wishlist-service/      Customer wishlists (8089)
coupon-service/        Coupon rules and validation (8090)
payment-service/       Idempotent simulated payments (8091)
address-service/       Customer address ownership (8092)
delivery-service/      Kafka-driven shipment tracking (8093)
admin-service/         Role-gated admin aggregator (8094)
contracts/             OpenAPI and event contracts
infrastructure/        Docker, database, Kafka, observability assets
docker-compose.yml
pom.xml
```

## V2 features

V2 adds trigram search, Redis catalog caching, reviews, wishlists, coupons, idempotent payments,
dedicated addresses, Kafka-driven delivery tracking, an RBAC admin dashboard, and metrics/tracing.
All internal mutation endpoints require `X-Internal-Token`; only the gateway and frontend are exposed
by the production Compose stack.

## Run locally

Prerequisites: Docker Desktop. For local builds, use JDK 21 and Node.js 22.22+.

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
npm run test:e2e  # requires the Docker backend stack
```

The backend suite includes a PostgreSQL Testcontainers concurrency test for customer-scoped checkout
idempotency. A full Docker smoke test has also verified order confirmation, stock decrement, duplicate
idempotency behavior, Kafka delivery, and notification persistence.

GitHub Actions runs `clean verify` for every backend module and performs a locked frontend install,
lint, and production build for pull requests and changes to the primary branch. After those gates pass,
Playwright starts the Docker Compose stack and verifies the complete customer journey in Chromium.

## Production upgrade

Production uses `compose.prod.yml`. On the EC2 host, pull the release, create `.env.production` from
`.env.production.example`, then run the one-time/rerunnable data preparation before starting V2:

```bash
chmod +x tools/prepare-v2-production.sh
./tools/prepare-v2-production.sh
docker compose --env-file .env.production -f compose.prod.yml up -d --build
curl -H "X-Internal-Token: $INTERNAL_API_TOKEN" -X POST http://localhost:8087/internal/search/reindex
```

The preparation script creates missing V2 databases and copies legacy Identity Service addresses into
Address Service without deleting the source data. Keep `.env.production` out of Git and use unique
values for the database password, JWT secret, internal token, and admin password.

## Next milestone

The next operational step is deploying this V2 stack to the existing EC2 host, validating all health
and Prometheus targets, running the browser journey against the public URL, and then tagging the V2
release. HTTPS/domain setup and managed secrets are recommended before public traffic.
