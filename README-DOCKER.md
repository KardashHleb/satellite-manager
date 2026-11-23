# Running in Docker: mission control center and mission service

The project consists of two Spring Boot applications in one multi-module Gradle repository:

| Module | Role | Default port | JAR |
|--------|------|-------------------|-----|
| **server** | REST API of the mission control center (satellites, constellations), Swagger UI | 8082 | `server.jar` |
| **mission-service** | Thin client: calls the center using the base URL from the environment | 8083 | `mission-service.jar` |

The center address is **not hard-coded**: `mission-service` uses the property `app.server.base-url`, which is set in Docker through the environment variable **`SERVER_URL`** (for example `http://server:8082`). The host name `server` is the container name in the Docker Compose user-defined network; the built-in DNS resolves it to the container IP.

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

Compose starts two **separate** services (two containers) in the shared `satellite-net` network:

- **server** — port mapping `8082:8082`, variable `SERVER_PORT=8082`, healthcheck on `GET /actuator/health`
- **mission-service** — `8083:8083`, `SERVER_PORT=8083`, `SERVER_URL=http://server:8082`, starts **after** the server becomes healthy (`depends_on: condition: service_healthy`)

The images are tagged with version **`1.0.0`** (see `image:` in `docker-compose.yml`). You can also tag an image with the commit, for example:

```bash
docker tag satellite-server:1.0.0 satellite-server:$(git rev-parse --short HEAD)
```

---

## Checking that it works

### 1. Swagger UI of the mission control center (from your laptop)

Open in a browser:

- [http://localhost:8082/swagger-ui/index.html](http://localhost:8082/swagger-ui/index.html)

(if the redirect is configured differently, try [http://localhost:8082/swagger-ui.html](http://localhost:8082/swagger-ui.html))

### 2. Health endpoints

- Server: `GET http://localhost:8082/actuator/health`
- Mission service: `GET http://localhost:8083/actuator/health`

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

The mission service calls the center using the internal URL and returns the same overview text:

```http
GET http://localhost:8083/api/remote/overview
```

After a successful start the `mission-service` log contains a message that the connection to `/api/overview` is established (the server is already up thanks to `depends_on` and the healthcheck).

---

## Running locally without Docker

From the root:

```bash
# Terminal 1 — the center
./gradlew :server:bootRun

# Terminal 2 — missions (by default expects the center at localhost:8082)
./gradlew :mission-service:bootRun
```

Default ports: server `8082`, missions `8083`. If the center runs at a different address:

```bash
set SERVER_URL=http://127.0.0.1:8082
./gradlew :mission-service:bootRun
```

(In PowerShell: `$env:SERVER_URL="http://127.0.0.1:8082"`)

---

## How the Docker files work

### Multi-stage build

- **build stage**: `eclipse-temurin:21-jdk-alpine`; `gradlew`, `settings.gradle`, `build.gradle` and the `server` and `mission-service` directories are copied into the context, then `./gradlew :<module>:bootJar -x test` runs.
- **run stage**: `eclipse-temurin:21-jre-alpine`; only the built JAR (`server.jar` or `mission-service.jar`) goes into the image, without sources and without whole build directories.

### Security

- The process in the container runs as the unprivileged user `app` (group `app`).
- `curl` is added to the final layer for the healthcheck and debugging.

### Files

- `server/Dockerfile` — image of the mission control center
- `mission-service/Dockerfile` — image of the mission service
- `docker-compose.yml` — network, ports, environment variables, server healthcheck and waiting for readiness in the mission service
- `.dockerignore` — excludes `build`, `.git` and other unneeded files from the build context

---

## Environment variables

| Variable | Used by | Purpose |
|------------|------------------|------------|
| `SERVER_PORT` | both applications | Port of the embedded Tomcat HTTP server |
| `SERVER_URL` | only `mission-service` | Base URL of the center (`http://<host_name>:<port>`), mapped to `app.server.base-url` |

In the `mission-service` code the base URL is read only from the Spring configuration (`@Value("${app.server.base-url}")`), which is bound to `SERVER_URL` in `application.properties`.

---

## Docker network

`docker-compose.yml` declares the user-defined network `satellite-net` (type `bridge`). The services join it explicitly; containers see each other by **service name** (`server`, `mission-service`), which is what `SERVER_URL=http://server:8082` relies on.

---

## Building the JARs without Docker

```bash
./gradlew :server:bootJar :mission-service:bootJar
```

Artifacts: `server/build/libs/server.jar`, `mission-service/build/libs/mission-service.jar`.

---

## Tests

```bash
./gradlew :server:test :mission-service:test
```
