# Running in Docker: mission control center, mission service and telemetry

The project is a multi-module Gradle repository with three Spring Boot applications and a shared module with the gRPC contracts:

| Module | Role | Default port | JAR |
|--------|------|-------------------|-----|
| **server** | REST API of the mission control center (satellites, constellations), Swagger UI, telemetry gRPC client | 8082 (HTTP) | `server.jar` |
| **mission-service** | Thin client: calls the center using the base URL from the environment | 8083 | `mission-service.jar` |
| **telemetry-service** | gRPC server with streaming telemetry (satellite temperatures) | 8084 (HTTP), **9091** (gRPC) | `telemetry-service.jar` |
| **telemetry-proto** | The single `telemetry.proto` and the generated Java/gRPC classes (a library, not an application) | — | — |
| **satellite-events** | JSON satellite events for Kafka (a library) | — | — |
| **kafka** (Compose) | Message broker | **9092** | — |

The center address is **not hard-coded** in `mission-service`: it uses the property `app.server.base-url`, which is set in Docker through **`SERVER_URL`** (for example `http://server:8082`). The host name `server` is the service name in the Docker Compose network.

The center connects to the telemetry service over gRPC using **`TELEMETRY_GRPC_ADDRESS`** (in compose: `static://telemetry-service:9091`).

---

## Requirements

- JDK 21 (for a local build without Docker)
- Docker Engine and Docker Compose v2

---

## Quick start: build and run

From the repository root:

```bash
docker compose up --build
```

Compose starts **PostgreSQL**, **Kafka** and three Spring Boot applications in the **`space-net`** network:

- **postgres** — PostgreSQL 16, database `satellite_db`, user `satellite`, volume `postgres-data`, healthcheck `pg_isready`
- **kafka** — Apache Kafka (KRaft, `apache/kafka:latest`), `9092:9092`, `SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:9092` for server and telemetry-service
- **telemetry-service** — `8084:8084`, `9091:9091`, subscribes to `satellite.created` / `satellite.deleted`, starts **after** Kafka is healthy
- **server** — `8082:8082`, `SPRING_DATASOURCE_*` → `postgres`, publishes satellite events to Kafka, `TELEMETRY_GRPC_ADDRESS` → `telemetry-service:9091`, starts **after** Postgres and Kafka are healthy
- **mission-service** — `8083:8083`, `SERVER_URL=http://server:8082`, starts **after** the server is healthy

The images are tagged with version **`1.0.0`** (see `image:` in `docker-compose.yml`). You can also tag an image with the commit:

```bash
docker tag satellite-server:1.0.0 satellite-server:$(git rev-parse --short HEAD)
```

---

## Checking that it works

### 1. Swagger UI of the mission control center (from your laptop)

- [http://localhost:8082/swagger-ui/index.html](http://localhost:8082/swagger-ui/index.html)

(if the redirect is configured differently: [http://localhost:8082/swagger-ui.html](http://localhost:8082/swagger-ui.html))

### 2. Health endpoints

- Server: `GET http://localhost:8082/actuator/health`
- Mission service: `GET http://localhost:8083/actuator/health`
- Telemetry: `GET http://localhost:8084/actuator/health`

### 3. A request to the server API (Postman / curl)

Constellation overview:

```http
GET http://localhost:8082/api/overview
```

List of constellations:

```http
GET http://localhost:8082/api/constellations
```

### 4. The mission service as a proxy to the center

```http
GET http://localhost:8083/api/remote/overview
```

After a successful start the `mission-service` log contains a message about the connection to the center (after `depends_on` and the server healthcheck).

### 5. Telemetry

**telemetry-service** streams updates over gRPC; **server** subscribes on startup and writes the temperatures to the database. The telemetry service has no separate public REST API for this — check it with the health endpoint (item 2) and the `server` log (`Telemetry gRPC client started`).

---

## Running locally without Docker

You need **PostgreSQL** (see `server/src/main/resources/application.yml`: by default `jdbc:postgresql://localhost:5432/satellite_db`, user `satellite`).

From the root:

```bash
# Terminal 1 — telemetry (gRPC 9091)
./gradlew :telemetry-service:bootRun

# Terminal 2 — the center (expects telemetry at localhost:9091 by default)
./gradlew :server:bootRun

# Terminal 3 — missions (center at localhost:8082)
./gradlew :mission-service:bootRun
```

Ports: server `8082`, missions `8083`, telemetry HTTP `8084` / gRPC `9091`.

If the center runs at a different address, for the missions (PowerShell):

```powershell
$env:SERVER_URL="http://127.0.0.1:8082"
./gradlew :mission-service:bootRun
```

If the telemetry service runs on another host, for the center:

```powershell
$env:TELEMETRY_GRPC_ADDRESS="static://127.0.0.1:9091"
./gradlew :server:bootRun
```

---

## How the Docker files work

### Multi-stage build

- **build stage**: `eclipse-temurin:21-jdk`; `gradlew`, `settings.gradle`, `build.gradle` and the modules `telemetry-proto`, `server`, `mission-service`, `telemetry-service` are copied, then `./gradlew :<module>:bootJar -x test` runs.
- **run stage**: `eclipse-temurin:21-jre-alpine`, only the built JAR.

### Security

- The process runs as the user `app`.
- `curl` is added to the final layer for the healthcheck.

### Files

- `server/Dockerfile` — mission control center
- `mission-service/Dockerfile` — mission service
- `telemetry-service/Dockerfile` — telemetry
- `docker-compose.yml` — network `space-net`, ports, variables, healthchecks
- `.dockerignore` — excludes `build`, `.git` and other unneeded files from the context

gRPC contract: **`telemetry-proto/src/main/proto/telemetry.proto`** (one source for `server` and `telemetry-service`).

---

## Environment variables

| Variable | Used by | Purpose |
|------------|------------------|------------|
| `SERVER_PORT` | all three applications | Tomcat HTTP port |
| `SERVER_URL` | `mission-service` | Base URL of the center → `app.server.base-url` |
| `TELEMETRY_GRPC_ADDRESS` | `server` | Telemetry gRPC address (compose: `static://telemetry-service:9091`) |
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | `server`, `telemetry-service` | Kafka address (compose: `kafka:9092`) |
| `APP_KAFKA_ENABLED` | `server` | Turn off publishing to Kafka (`false` in tests) |
| `SPRING_DATASOURCE_URL` | `server` | PostgreSQL JDBC URL |
| `SPRING_DATASOURCE_USERNAME` | `server` | database user |
| `SPRING_DATASOURCE_PASSWORD` | `server` | database password |
| `APP_CONSOLE_MENU_ENABLED` | `server` | console menu (`false` in Docker) |

---

## Docker network

`docker-compose.yml` declares the network **`space-net`** (`bridge`). The services see each other by name: `postgres`, `server`, `mission-service`, `telemetry-service`.

---

## Building the JARs without Docker

```bash
./gradlew :server:bootJar :mission-service:bootJar :telemetry-service:bootJar
```

Artifacts: `server/build/libs/server.jar`, `mission-service/build/libs/mission-service.jar`, `telemetry-service/build/libs/telemetry-service.jar`.

---

## Tests

```bash
./gradlew :telemetry-proto:build :server:test :mission-service:test :telemetry-service:test
```
