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
- `gradle-wrapper.jar` was missing from Git; **fixed in the commit that added this file**.
