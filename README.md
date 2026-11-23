# Satellite Manager

A multi-module Gradle project: a space operation center for satellite constellations, a mission service and gRPC
telemetry streaming.

## Quick start (Docker)

**Requirements:** Docker Engine, Docker Compose v2.

From the repository root:

```bash
docker compose up --build
```

This starts PostgreSQL, Kafka and three Spring Boot applications in the `space-net` network.

| Service | Port | Purpose |
|--------|------|------------|
| **server** | 8082 | REST API, Swagger UI |
| **mission-service** | 8083 | Proxy to the space operation center |
| **telemetry-service** | 8084 (HTTP), 9091 (gRPC) | Telemetry stream |
| **postgres** | — | Database `satellite_db` |
| **kafka** | 9092 | Satellite lifecycle events |

Check:

- Swagger: [http://localhost:8082/swagger-ui/index.html](http://localhost:8082/swagger-ui/index.html)
- Health: `GET /actuator/health` on ports 8082, 8083, 8084

More about Docker, environment variables and running locally without containers: [docs/AdditionalREADME/README-DOCKER.md](docs/AdditionalREADME/README-DOCKER.md).

## Project structure

```
satellite-manager/
├── server/                 # Space operation center (REST, JPA, Kafka producer, gRPC client)
├── mission-service/        # Client of server via SERVER_URL
├── telemetry-service/      # gRPC telemetry server, Kafka consumer
├── telemetry-proto/        # Shared gRPC contract (telemetry.proto)
├── satellite-events/       # JSON event models for Kafka
├── docker-compose.yml
└── docs/                   # Additional documentation
```

## Main dependencies

| Technology | Version / note |
|------------|---------------------|
| Java | 21 |
| Spring Boot | 3.4.x |
| PostgreSQL | 16 (in Docker) |
| Apache Kafka | KRaft (in Docker) |
| gRPC | `telemetry-proto`, grpc-spring-boot-starter |
| Flyway | Database migrations |
| Gradle | Builds all modules |

## Outbox and Inbox (Kafka)

Satellite created and deleted events (`satellite.created`, `satellite.deleted`) are delivered through Kafka
consistently with the database:

- **Outbox (server)**: the event is written to the `outbox` table in the same transaction as the satellite change;
  a background relay sends the rows to Kafka.
- **Inbox (telemetry-service)**: incoming messages are recorded in the `inbox` table by `eventId`; a repeated
  delivery is not processed twice.

Message format and configuration: [docs/KAFKA_SATELLITE_EVENTS.md](docs/KAFKA_SATELLITE_EVENTS.md).

## Build without Docker

```bash
./gradlew :server:bootJar :mission-service:bootJar :telemetry-service:bootJar
```

Tests:

```bash
./gradlew test
```
