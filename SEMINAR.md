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

- The console menu item "Emergency satellite charging" only prints which satellites would be charged; it does not change the battery level.

## Fixed after the seminar

- `gradle-wrapper.jar` was missing from Git (this broke `./gradlew` and the Docker build), and the jar added later had no `Main-Class`; it is now the Gradle 8.14 wrapper jar.
- `./gradlew :server:bootRun` failed with "No line found" in the console menu; `bootRun` now forwards `System.in`.
- The console menu asked to press Enter before and after the start-up banner and printed the repository contents twice on exit; it now asks once and prints them once.
- `POST /api/add-satellites` always failed: `@JsonTypeInfo` consumes the JSON `type` property, so the no-arg constructors of the parameter classes left `type` null and no factory matched; they now set the type.
- `SpaceOperationController` answered every error with 400 and emoji strings; it now returns 404 for an unknown constellation or satellite (new `NotFoundException`), 400 for bad input, 500 otherwise, without emojis (test: `SpaceOperationControllerTest`).
- `Satellite.activate()` on an already active satellite printed "battery level is low", and the console "activate by type" counted satellites that failed to activate; it now reports "already active" and counts only satellites that were turned on (test: `SatelliteActivationTest`).
- The console "add a new satellite" menu accepted a battery level outside 0.0-1.0, which crashed with an exception; it now asks again.
- `SatelliteParam.toString()` printed the 0..1 battery level as a percentage (for example "0.8%"); it now prints "80%".
- All text (docs, comments, messages, test names) translated to English.
