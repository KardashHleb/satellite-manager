# Kafka: satellite lifecycle events

## Purpose

**server** publishes events when a satellite is successfully **created** or **deleted**.  
**telemetry-service** subscribes to the topics and keeps a registry of known satellites for the gRPC telemetry stream.

## Topics

| Topic | When it is published |
|-------|-------------------|
| `satellite.created` | The satellite is saved to the database (added to a constellation) |
| `satellite.deleted` | The satellite is deleted by id or together with its constellation |

The topics are created automatically (`KAFKA_AUTO_CREATE_TOPICS_ENABLE` in `docker-compose.yml`).

## Message format (JSON)

```json
{
  "eventType": "CREATED",
  "satelliteId": 1,
  "satelliteName": "Comm-1",
  "constellationName": "RU Basic",
  "satelliteType": "COMMUNICATION",
  "occurredAt": "2026-05-22T14:30:00.123456789Z"
}
```

| Field | Description |
|------|----------|
| `eventType` | `CREATED` or `DELETED` |
| `satelliteId` | database id of the saved satellite |
| `satelliteName` | satellite name |
| `constellationName` | constellation name |
| `satelliteType` | `COMMUNICATION` or `IMAGE` |
| `occurredAt` | ISO-8601 instant (`Instant.now()`) |

Class: `com.satellite.events.SatelliteLifecycleEvent` (module **satellite-events**).

Kafka message key: `satelliteId` (as a string).

## Spring configuration

### server (producer)

`application.yml`:

```yaml
spring.kafka.bootstrap-servers: ${SPRING_KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
app.kafka.enabled: ${APP_KAFKA_ENABLED:true}
```

Sending: `KafkaSatelliteEventPublisher` → `KafkaTemplate`.  
Call sites: `ConstellationService.addSatellite`, `CrudManagementService.deleteSatellite`, `deleteConstellation`.

In tests: `app.kafka.enabled: false` → `NoOpSatelliteEventPublisher`.

### telemetry-service (consumer)

```yaml
spring.kafka.bootstrap-servers: ${SPRING_KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
spring.kafka.consumer.group-id: telemetry-service
```

Listeners: `SatelliteLifecycleKafkaListener` (`@KafkaListener` on both topics).

Checking the registry: `GET http://localhost:8084/api/telemetry/known-satellites`

## Docker Compose

Service **kafka** (Apache Kafka, KRaft, port **9092**).  
Variable for **server** and **telemetry-service**:

```text
SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:9092
```

## Running locally

1. `docker compose up kafka` (or the whole stack)
2. `./gradlew :telemetry-service:bootRun`
3. `./gradlew :server:bootRun`

After the server starts (initialization of "RU Basic"), the telemetry log shows CREATED events, and `known-satellites` lists the satellite names.
