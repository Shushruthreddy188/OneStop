<div align="center">

# 🛒 OneStop

### Enterprise-Grade Online Retail Platform built with Microservices

A cloud-native e-commerce platform demonstrating **real-world distributed systems**, **event-driven architecture**, **Spring Boot microservices**, **React**, **Kafka**, **Redis**, **Docker**, and **AWS deployment**.

![Java](https://img.shields.io/badge/Java-21-red)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3-brightgreen)
![React](https://img.shields.io/badge/React-19-61DAFB)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue)
![Kafka](https://img.shields.io/badge/Kafka-Event--Driven-black)
![Redis](https://img.shields.io/badge/Redis-Cache-red)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED)
![AWS](https://img.shields.io/badge/AWS-EC2-orange)
![License](https://img.shields.io/badge/License-MIT-green)

</div>

---

# 📖 Overview

OneStop is a production-style online retail platform designed using a **microservices architecture**.

Unlike traditional CRUD applications, OneStop demonstrates many patterns used in enterprise systems:

- Distributed microservices
- API Gateway
- Event-driven communication with Kafka
- Database-per-service architecture
- JWT Authentication
- Redis caching
- Transactional Outbox Pattern
- Idempotent Checkout
- Distributed Inventory Reservation
- Dockerized deployment
- AWS deployment

The project began as a simple MVP and evolved into a scalable retail platform with **15 independent services**.

---

# 🏗 Architecture

```
                    React Frontend
                           │
                           ▼
                  Spring Cloud Gateway
                           │
 ───────────────────────────────────────────────────────

 Identity Service
 Catalog Service
 Cart Service
 Inventory Service
 Order Service
 Notification Service
 Search Service
 Review Service
 Wishlist Service
 Coupon Service
 Payment Service
 Address Service
 Delivery Service
 Admin Service

 ───────────────────────────────────────────────────────

 PostgreSQL
 Kafka
 Redis

```

Each microservice owns:

- its own database
- business logic
- REST APIs
- Flyway migrations
- deployment lifecycle

---

# ✨ Features

## Customer Features

- User Registration & Login
- JWT Authentication
- Product Catalog
- Categories & Brands
- Typo-tolerant Search
- Product Reviews
- Wishlist
- Shopping Cart
- Checkout
- Coupon Validation
- Payment Simulation
- Order History
- Order Cancellation
- Shipment Tracking
- Address Management

---

## Backend Features

- Spring Cloud Gateway
- Transactional Outbox Pattern
- Idempotent Checkout
- Inventory Reservation
- Kafka Event Publishing
- Kafka Consumers
- Redis Catalog Cache
- Retry & Recovery Workers
- PostgreSQL Database Per Service
- Internal Service Authentication
- Admin Dashboard APIs

---

# 🚀 Tech Stack

## Frontend

- React
- TypeScript
- Vite
- React Router
- TanStack Query
- React Hook Form

## Backend

- Java 21
- Spring Boot 3
- Spring Security
- Spring Data JPA
- Spring Cloud Gateway
- Flyway

## Database

- PostgreSQL
- Redis

## Messaging

- Apache Kafka

## Observability

- Prometheus
- Grafana
- Zipkin

## DevOps

- Docker
- Docker Compose
- GitHub Actions
- AWS EC2
- Nginx

---

# 🧩 Microservices

| Service | Port | Responsibility |
|----------|------|---------------|
| API Gateway | 8080 | Entry Point |
| Identity Service | 8081 | Authentication & Users |
| Catalog Service | 8082 | Products |
| Cart Service | 8083 | Shopping Cart |
| Inventory Service | 8084 | Stock Management |
| Order Service | 8085 | Checkout |
| Notification Service | 8086 | Kafka Notifications |
| Search Service | 8087 | Search & Autocomplete |
| Review Service | 8088 | Ratings & Reviews |
| Wishlist Service | 8089 | Wishlists |
| Coupon Service | 8090 | Coupons |
| Payment Service | 8091 | Payments |
| Address Service | 8092 | Addresses |
| Delivery Service | 8093 | Shipment Tracking |
| Admin Service | 8094 | Admin Dashboard |

---

# 🔄 Checkout Workflow

```
Customer
    │
    ▼
API Gateway
    │
    ▼
Order Service
    │
    ├────────► Inventory Service
    │               │
    │         Reserve Stock
    │
    ├────────► Payment Service
    │
    ▼
Confirm Order
    │
    ▼
Transactional Outbox
    │
    ▼
Kafka
    │
    ├────────► Notification Service
    │
    └────────► Delivery Service
```

---

# 🔐 Reliability

OneStop implements several enterprise reliability patterns.

- Transactional Outbox
- Customer-scoped Idempotency
- Atomic Inventory Reservation
- Retry with Exponential Backoff
- Kafka Consumer Deduplication
- Reconciliation Worker
- Database Constraints
- Internal Service Authentication

---

# 📂 Repository Structure

```
frontend/

api-gateway/

identity-service/

catalog-service/

cart-service/

inventory-service/

order-service/

notification-service/

search-service/

review-service/

wishlist-service/

coupon-service/

payment-service/

address-service/

delivery-service/

admin-service/

contracts/

infrastructure/

docker-compose.yml
```

---

# ⚙ Running Locally

## Prerequisites

- Java 21
- Node.js 22+
- Docker Desktop

Clone

```bash
git clone https://github.com/Shushruthreddy188/OneStop.git

cd OneStop
```

Start everything

```bash
docker compose up --build
```

Frontend

```
http://localhost:5173
```

API Gateway

```
http://localhost:8080
```

---

# 🧪 Testing

Backend

```bash
./mvnw test
```

Frontend

```bash
cd frontend

npm install

npm run build

npm run test:e2e
```

The project includes:

- Unit Tests
- Integration Tests
- Testcontainers
- End-to-End Tests
- Concurrency Tests

---

# 📊 Current Status

| Component | Status |
|-----------|--------|
| MVP | ✅ Complete |
| V2 Features | ✅ Complete |
| Docker Deployment | ✅ |
| Kafka Integration | ✅ |
| Redis Cache | ✅ |
| AWS Deployment | ✅ |
| CI/CD | ✅ |
| Monitoring | ✅ Local |
| Production HTTPS | ✅ |

---

# 🛣 Roadmap

### ✅ V1

- Authentication
- Catalog
- Cart
- Inventory
- Orders
- Notifications

### ✅ V2

- Search
- Reviews
- Wishlist
- Coupons
- Payment
- Address Service
- Delivery Tracking
- Admin Dashboard
- Redis Cache
- Kafka
- Observability

### 🚀 V3

- Recommendation Engine
- AI Search
- Loyalty Program
- Marketplace
- Books
- Electronics
- Liquor
- Recipe Service

---

# 📚 Documentation

- MVP Architecture
- V2 Architecture
- Production Deployment
- AWS Infrastructure
- CI/CD Pipeline
- Operations Guide

---

# 🤝 Contributing

Contributions are welcome.

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Open a Pull Request

---

# 📄 License

MIT License.

---

<div align="center">

**Built with Java, Spring Boot, React, Kafka, Redis, Docker, and AWS**

⭐ Star this repository if you found it helpful.

</div>
