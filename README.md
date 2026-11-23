
A multi-module Gradle project:

- **server** — mission control center (REST, Swagger, JPA + PostgreSQL, telemetry gRPC client);
- **mission-service** — a client of the center, configured with `SERVER_URL`;
- **telemetry-service** — gRPC telemetry stream;
- **telemetry-proto** — the shared `telemetry.proto` contract for server and telemetry-service;
- **satellite-events** — JSON satellite events for Kafka.

Satellite created/deleted events (Kafka): [docs/KAFKA_SATELLITE_EVENTS.md](docs/KAFKA_SATELLITE_EVENTS.md).

Detailed instructions on Docker, environment variables and how to check that everything works are in [README-DOCKER.md](README-DOCKER.md).

Default ports: center — **8082**, missions — **8083**, telemetry HTTP — **8084**, gRPC — **9091**.

**Database:** start PostgreSQL locally (for example `localhost:5432`, database `satellite_db`, user/password `satellite`/`satellite`, as in `application.yml`) or use `docker compose up` (the compose file includes a `postgres` service). The schema is created by **Flyway** (`server/src/main/resources/db/migration`), Hibernate runs with `ddl-auto: validate`.

The assignment question about **@Embedded** vs **@OneToOne** is answered in [docs/JPA_EMBEDDED_VS_ONETOONE.md](docs/JPA_EMBEDDED_VS_ONETOONE.md).
