# Running in Docker: mission control center, mission service and telemetry

The project is a Gradle multi-module repository with three Spring Boot applications and a shared gRPC contracts module:

| Module | Role | Default port | JAR |
|--------|------|-------------------|-----|
| **server** | Mission control center REST API (satellites, constellations), Swagger UI, telemetry gRPC client | 8082 (HTTP) | `server.jar` |
| **mission-service** | Thin client: calls the control center at a base URL taken from the environment | 8083 | `mission-service.jar` |
| **telemetry-service** | Streaming telemetry gRPC server (satellite temperatures) | 8084 (HTTP), **9091** (gRPC) | `telemetry-service.jar` |
| **telemetry-proto** | The single `telemetry.proto` and the generated Java/gRPC classes (a library, not an application) | — | — |
| **satellite-events** | JSON satellite events for Kafka (library) | — | — |
| **kafka** (Compose) | Message broker | **9092** | — |

The control center address is **not hardcoded** in `mission-service`: it uses the `app.server.base-url` property, which in Docker is set via **`SERVER_URL`** (for example `http://server:8082`). The host name `server` is the service name in the Docker Compose network.

The control center connects to telemetry over gRPC via **`TELEMETRY_GRPC_ADDRESS`** (in compose: `static://telemetry-service:9091`).

---

## Requirements

- JDK 21 (for local builds without Docker)
- Docker Engine and Docker Compose v2

---

## Quick start: build and run

From the repository root:

```bash
docker compose up --build
```

Compose starts **PostgreSQL**, **Kafka** and three Spring Boot applications on the **`space-net`** network:

- **postgres** — PostgreSQL 16, database `satellite_db`, user `satellite`, volume `postgres-data`, healthcheck `pg_isready`
- **kafka** — Apache Kafka (KRaft, `apache/kafka:3.9.0`), `9092:9092`, `SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:9092` for server and telemetry-service
- **telemetry-service** — `8084:8084`, `9091:9091`, subscribes to `satellite.created` / `satellite.deleted`, starts **after** Kafka is healthy
- **server** — `8082:8082`, `SPRING_DATASOURCE_*` → `postgres`, publishes satellite events to Kafka, `TELEMETRY_GRPC_ADDRESS` → `telemetry-service:9091`, starts **after** Postgres and Kafka are healthy
- **mission-service** — `8083:8083`, `SERVER_URL=http://server:8082`, starts **after** the server is healthy

Images are tagged with version **`1.0.0`** (see `image:` in `docker-compose.yml`). You can additionally tag an image with the commit:

```bash
docker tag satellite-server:1.0.0 satellite-server:$(git rev-parse --short HEAD)
```

---

## Checking that it works

### 1. Mission control center Swagger UI (from your laptop)

- [http://localhost:8082/swagger-ui/index.html](http://localhost:8082/swagger-ui/index.html)

(if the redirect is configured differently: [http://localhost:8082/swagger-ui.html](http://localhost:8082/swagger-ui.html))

### 2. Health endpoints

- Server: `GET http://localhost:8082/actuator/health`
- Mission service: `GET http://localhost:8083/actuator/health`
- Telemetry: `GET http://localhost:8084/actuator/health`

### 3. Server API request (Postman / curl)

Constellation overview:

```http
GET http://localhost:8082/api/overview
```

List of constellations:

```http
GET http://localhost:8082/api/constellations
```

### 4. Mission service as a proxy to the control center

```http
GET http://localhost:8083/api/remote/overview
```

On a successful start, the `mission-service` logs will contain a message about the connection to the control center (after `depends_on` and the server healthcheck).

### 5. Telemetry

**telemetry-service** provides a stream of updates over gRPC; **server** subscribes on startup and writes temperatures to the database. Telemetry has no separate public REST API — check it via health (item 2) and the `server` logs (`Telemetry gRPC client started`).

---

## Running locally without Docker

You need **PostgreSQL** (see `server/src/main/resources/application.yml`: by default `jdbc:postgresql://localhost:5432/satellite_db`, user `satellite`).

From the root:

```bash
# Terminal 1 — telemetry (gRPC 9091)
./gradlew :telemetry-service:bootRun

# Terminal 2 — control center (expects telemetry on localhost:9091 by default)
./gradlew :server:bootRun

# Terminal 3 — missions (control center on localhost:8082)
./gradlew :mission-service:bootRun
```

Ports: server `8082`, missions `8083`, telemetry HTTP `8084` / gRPC `9091`.

For missions with a different control center address (PowerShell):

```powershell
$env:SERVER_URL="http://127.0.0.1:8082"
./gradlew :mission-service:bootRun
```

For the control center with telemetry on another host:

```powershell
$env:TELEMETRY_GRPC_ADDRESS="static://127.0.0.1:9091"
./gradlew :server:bootRun
```

---

## How the Docker files are structured

### Multi-stage build

- **build stage**: `eclipse-temurin:21-jdk`, copies `gradlew`, `settings.gradle`, `build.gradle`, the `telemetry-proto`, `server`, `mission-service`, `telemetry-service` modules, runs `./gradlew :<module>:bootJar -x test`.
- **run stage**: `eclipse-temurin:21-jre-alpine`, only the built JAR.

### Security

- The process runs as the `app` user.
- `curl` is added to the final layer for the healthcheck.

### Files

- `server/Dockerfile` — mission control center
- `mission-service/Dockerfile` — mission service
- `telemetry-service/Dockerfile` — telemetry
- `docker-compose.yml` — `space-net` network, ports, variables, healthchecks
- `.dockerignore` — excludes `build`, `.git` and other unneeded files from the context

gRPC contract: **`telemetry-proto/src/main/proto/telemetry.proto`** (a single source for `server` and `telemetry-service`).

---

## Environment variables

| Variable | Used by | Purpose |
|------------|------------------|------------|
| `SERVER_PORT` | all three applications | Tomcat HTTP port |
| `SERVER_URL` | `mission-service` | Control center base URL → `app.server.base-url` |
| `TELEMETRY_GRPC_ADDRESS` | `server` | Telemetry gRPC address (compose: `static://telemetry-service:9091`) |
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | `server`, `telemetry-service` | Kafka address (compose: `kafka:9092`) |
| `APP_KAFKA_ENABLED` | `server` | Disable publishing to Kafka (`false` in tests) |
| `SPRING_DATASOURCE_URL` | `server` | PostgreSQL JDBC URL |
| `SPRING_DATASOURCE_USERNAME` | `server` | database user |
| `SPRING_DATASOURCE_PASSWORD` | `server` | database password |
| `APP_CONSOLE_MENU_ENABLED` | `server` | console menu (`false` in Docker) |

---

## Docker network

`docker-compose.yml` defines the **`space-net`** network (`bridge`). Services reach each other by name: `postgres`, `server`, `mission-service`, `telemetry-service`.

---

## Building JARs without Docker

```bash
./gradlew :server:bootJar :mission-service:bootJar :telemetry-service:bootJar
```

Artifacts: `server/build/libs/server.jar`, `mission-service/build/libs/mission-service.jar`, `telemetry-service/build/libs/telemetry-service.jar`.

---

## Tests

```bash
./gradlew :telemetry-proto:build :server:test :mission-service:test :telemetry-service:test
```
