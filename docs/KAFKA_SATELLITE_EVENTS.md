# Kafka: satellite lifecycle events

## Purpose

**server** publishes events when a satellite is successfully **created** or **deleted**.  
**telemetry-service** subscribes to the topics and maintains a registry of known satellites for the gRPC telemetry stream.

## Topics

| Topic | When it is published |
|-------|----------------------|
| `satellite.created` | A satellite was saved to the DB (added to a constellation) |
| `satellite.deleted` | A satellite was deleted by id or together with its constellation |

Topics are created automatically (`KAFKA_CFG_AUTO_CREATE_TOPICS_ENABLE` in Docker).

## Message format (JSON)

```json
{
  "eventId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "eventType": "CREATED",
  "satelliteId": 1,
  "satelliteName": "Comm-1",
  "constellationName": "RU Basic",
  "satelliteType": "COMMUNICATION",
  "occurredAt": "2026-05-22T14:30:00.123456789Z"
}
```

| Field | Description |
|-------|-------------|
| `eventId` | Event UUID (inbox idempotency on the telemetry side) |
| `eventType` | `CREATED` or `DELETED` |
| `satelliteId` | DB id (can be `null` only before flush; in production it is set after save) |
| `satelliteName` | satellite name |
| `constellationName` | constellation name |
| `satelliteType` | `COMMUNICATION` or `IMAGE` |
| `occurredAt` | ISO-8601 instant (`Instant.now()`) |

Class: `com.satellite.events.SatelliteLifecycleEvent` (module **satellite-events**).

Kafka message key: `satelliteId` (string).

## Spring configuration

### server (producer)

`application.yml`:

```yaml
spring.kafka.bootstrap-servers: ${SPRING_KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
app.kafka.enabled: ${APP_KAFKA_ENABLED:true}
```

Sending: **Transactional Outbox** — `OutboxSatelliteEventPublisher` writes to the `outbox` table in the same transaction as the satellite change; `OutboxRelayScheduler` sends `PENDING` records to Kafka via `KafkaTemplate` every 5 s.  
Call sites: `ConstellationService.addSatellite`, `CrudManagementService.deleteSatellite`, `deleteConstellation`.

In tests: `app.kafka.enabled: false` → `NoOpSatelliteEventPublisher`.

### telemetry-service (consumer)

```yaml
spring.kafka.bootstrap-servers: ${SPRING_KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
spring.kafka.consumer.group-id: telemetry-service
```

Listeners: `SatelliteLifecycleKafkaListener` → `SatelliteLifecycleEventProcessor` (**Inbox**: a record is written to the `inbox` table by `eventId`, duplicates are processed idempotently).

Checking the registry: `GET http://localhost:8084/api/telemetry/known-satellites`

## Docker Compose

The **kafka** service (Apache Kafka, KRaft, port **9092**).  
Variable for **server** and **telemetry-service**:

```text
SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:9092
```

## Running locally

1. `docker compose up kafka` (or the whole stack)
2. `./gradlew :telemetry-service:bootRun`
3. `./gradlew :server:bootRun`

After server starts (initializing "RU Basic"), CREATED events will appear in the telemetry logs, and `known-satellites` will list the satellite names.
