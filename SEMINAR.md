# Seminar 9 — Microservices with Docker and Docker Compose

| | |
|---|---|
| Branch | `seminar_9` |
| Based on | [`seminar_7`](https://github.com/KardashHleb/satellite-manager/tree/seminar_7), contains the MVC work of [`seminar_8`](https://github.com/KardashHleb/satellite-manager/tree/seminar_8) |
| Last commit | 2026-04-18 |
| Next | [`seminar_9.2`](https://github.com/KardashHleb/satellite-manager/tree/seminar_9.2) (database), [`seminar_9_postmanExample`](https://github.com/KardashHleb/satellite-manager/tree/seminar_9_postmanExample) (Postman) |
| All seminars | [SEMINARS.md on main](https://github.com/KardashHleb/satellite-manager/blob/main/SEMINARS.md) |

## Learning goal

Split the application into **two services** that talk over HTTP, package each one as a **Docker image**, and start
the whole system with **Docker Compose**.

## What was done

- **Multi-module Gradle build** (`settings.gradle`): `server` and `mission-service`. The code moved from `src/` to `server/`.
- **`server`**: `SatelliteServerApplication`, a `ConsoleMenuRunner` that can be switched off with
  `APP_CONSOLE_MENU_ENABLED=false` (needed inside a container), `ConstellationDataInitializer` with demo data,
  `AspectProxyConfig`, Spring Boot Actuator.
- **`mission-service`**: `MissionGatewayController` exposes `GET /api/remote/overview`, which calls the server's
  `/api/overview` through `SpaceCenterGatewayService`. The server address comes from `SERVER_URL`.
  `ServerConnectivityProbe` checks the connection once at startup.
- **Docker**: multi-stage `server/Dockerfile` and `mission-service/Dockerfile` (JDK image to build, JRE image to run,
  non-root user), `.dockerignore`.
- **`docker-compose.yml`**: network `satellite-net`; `mission-service` starts only after `server` is healthy
  (actuator health check).
- `README-DOCKER.md` with Docker instructions.

## How to run

```bash
docker compose up --build
```

or without Docker:

```bash
./gradlew :server:bootRun
./gradlew :mission-service:bootRun
```

- Swagger UI: http://localhost:8082/swagger-ui/index.html
- Health: http://localhost:8082/actuator/health, http://localhost:8083/actuator/health
- Call through the second service: http://localhost:8083/api/remote/overview
- Tests: `./gradlew :server:test :mission-service:test`

## Known issues

- The Docker images run `./gradlew` inside the build stage, so they could not be built while
  `gradle-wrapper.jar` was missing from Git. **Fixed in the commit that added this file.**
