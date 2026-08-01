# Contracts

Cross-service agreements, versioned independently of any single service.

- `openapi/` — OpenAPI/Swagger specifications for each service's public API.
  Each service also serves its live spec at `/v3/api-docs` and Swagger UI at
  `/swagger-ui.html`; exported snapshots live here for review and codegen.
- `events/` — event schemas (introduced with Kafka in Milestone 6).
