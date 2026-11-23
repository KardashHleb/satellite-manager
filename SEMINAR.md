# Seminar 11 — Event-driven communication with Kafka

| | |
|---|---|
| Branch | `seminar_11` |
| Based on | [`seminar_9.2`](https://github.com/KardashHleb/satellite-manager/tree/seminar_9.2), includes the gRPC commit of [`seminar_10`](https://github.com/KardashHleb/satellite-manager/tree/seminar_10) |
| Last commit | 2026-05-23 |
| Next | [`seminar_11_api_automation`](https://github.com/KardashHleb/satellite-manager/tree/seminar_11_api_automation) |
| All seminars | [SEMINARS.md on main](https://github.com/KardashHleb/satellite-manager/blob/main/SEMINARS.md) |

## Learning goal

Decouple services with **asynchronous events**: publish satellite lifecycle events to **Apache Kafka** and let
another service react to them, and clean up the gRPC setup with a shared contract module.

## What was done

- **New shared modules**:
  - `telemetry-proto` — the single `telemetry.proto` contract, compiled once and used by both sides;
  - `satellite-events` — `SatelliteLifecycleEvent` (JSON record) and `SatelliteKafkaTopics`
    (`satellite.created`, `satellite.deleted`).
- **Producer (`server`)**: `KafkaProducerConfig`; package `kafka/` with the `SatelliteEventPublisher` interface,
  `KafkaSatelliteEventPublisher`, a `NoOpSatelliteEventPublisher` for runs without Kafka (`app.kafka.enabled=false`),
  and `SatelliteEventMapper`. Events are sent when a satellite is added or deleted.
- **Consumer (`telemetry-service`)**: `SatelliteLifecycleKafkaListener` updates a `SatelliteRegistry`; the gRPC
  stream now sends telemetry for the satellites from the registry instead of hard-coded names.
  New endpoint `GET /api/telemetry/known-satellites`.
- **Compose**: Kafka in KRaft mode (no ZooKeeper) on port 9092 with a health check.
- **Docs and tests**: `docs/KAFKA_SATELLITE_EVENTS.md`, README and the JPA document restored;
  `SatelliteEventMapperTest`.

## How to run

```bash
docker compose up --build
```

Add or delete a satellite through Swagger (http://localhost:8082/swagger-ui/index.html), then check
http://localhost:8084/api/telemetry/known-satellites.

Tests:

```bash
./gradlew :telemetry-proto:build :server:test :mission-service:test :telemetry-service:test
```

## Known issues

- Old copies of `telemetry.proto` remain in `server/` and `telemetry-service/` although only `telemetry-proto` is used.
- The Kafka image is not pinned (`apache/kafka:latest`).
- An event is sent directly after the database write, so it can be lost if Kafka is down; seminar 12 solves this
  with the transactional outbox.
- Kafka advertises only `kafka:9092`, so the server and telemetry service started on the host with `bootRun`
  (default `localhost:9092`) cannot use the Kafka from `docker compose up kafka`; run the whole stack in Compose or add a
  host listener.
- The console menu item "Emergency satellite charging" only prints which satellites would be charged; it does not change the battery level.

## Fixed after the seminar

- `gradle-wrapper.jar` was missing from Git (this broke `./gradlew` and the Docker build), and the jar added later had no `Main-Class`; it is now the Gradle 8.14 wrapper jar.
- Satellite operations (activate, deactivate, photo, send data, single-satellite mission) changed a detached copy, so with `open-in-view: false` nothing was saved; `ConstellationService` now changes the managed instance (test: `SatelliteOperationsPersistenceTest`).
- `POST /api/crud/satellites` returned the new satellite without its id; `CrudManagementService` now reads the saved satellite back.
- `SpaceOperationController` answered every error with 400 and emoji strings; it now returns 404 for an unknown constellation or satellite, 400 for bad input, 500 otherwise, without emojis.
- The console "test data transmission" and "test imaging" called the satellite directly and their result was not saved; they now go through the service.
- `Satellite.activate()` on an already active satellite printed "battery level is low", and the console "activate by type" counted satellites that failed to activate; it now reports "already active" and counts only satellites that were turned on.
- `./gradlew :server:bootRun` did not pass the console input to the application, so the console menu stopped at the first prompt; the `JavaExec` tasks now use `standardInput = System.in`.
- The `satellite.created` event was built from the unsaved satellite object, so `satelliteId` was always `null` (and the Kafka key fell back to the name); `ConstellationService.addSatellite` now publishes the saved copy (test: `SatelliteEventPublishingTest`).
- `docs/KAFKA_SATELLITE_EVENTS.md` named the wrong Compose variable for topic auto-creation; it now names `KAFKA_AUTO_CREATE_TOPICS_ENABLE`.
- All text (docs, comments, messages, test names) translated to English. The demo satellites are now `Comm-1`, `Comm-2`, `Imager-1` ... The two comments in the Flyway migration `V1__init_schema.sql` are kept in the original language on purpose: editing an applied migration changes its checksum, and Flyway would refuse to start on an existing database.
