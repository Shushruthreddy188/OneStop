# Infrastructure

Local runtime and platform assets.

- `docker/` — shared multi-stage `service.Dockerfile` used to build every backend module.
- `database/` — PostgreSQL init scripts (`init/` creates one database per service).
- `kafka/` — reserved for Milestone 6 (asynchronous notifications).
- `observability/` — reserved for Milestone 7 (tracing, metrics, logs).
