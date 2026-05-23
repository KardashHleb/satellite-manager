# Kafka: события жизненного цикла спутника

## Назначение

**server** публикует события при успешном **создании** и **удалении** спутника.  
**telemetry-service** подписывается на топики и ведёт реестр известных спутников для gRPC-потока телеметрии.

## Топики

| Топик | Когда публикуется |
|-------|-------------------|
| `satellite.created` | Спутник сохранён в БД (добавление в группировку) |
| `satellite.deleted` | Спутник удалён по id или вместе с удалением группировки |

Топики создаются автоматически (`KAFKA_CFG_AUTO_CREATE_TOPICS_ENABLE` в Docker).

## Формат сообщения (JSON)

```json
{
  "eventType": "CREATED",
  "satelliteId": 1,
  "satelliteName": "Связь-1",
  "constellationName": "RU Basic",
  "satelliteType": "COMMUNICATION",
  "occurredAt": "2026-05-22T14:30:00.123456789Z"
}
```

| Поле | Описание |
|------|----------|
| `eventType` | `CREATED` или `DELETED` |
| `satelliteId` | id в БД (может быть `null` только до flush; в продакшене — после save) |
| `satelliteName` | имя спутника |
| `constellationName` | имя группировки |
| `satelliteType` | `COMMUNICATION` или `IMAGE` |
| `occurredAt` | ISO-8601 instant (`Instant.now()`) |

Класс: `com.satellite.events.SatelliteLifecycleEvent` (модуль **satellite-events**).

Ключ сообщения в Kafka: `satelliteId` (строка).

## Конфигурация Spring

### server (producer)

`application.yml`:

```yaml
spring.kafka.bootstrap-servers: ${SPRING_KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
app.kafka.enabled: ${APP_KAFKA_ENABLED:true}
```

Отправка: `KafkaSatelliteEventPublisher` → `KafkaTemplate`.  
Точки вызова: `ConstellationService.addSatellite`, `CrudManagementService.deleteSatellite`, `deleteConstellation`.

В тестах: `app.kafka.enabled: false` → `NoOpSatelliteEventPublisher`.

### telemetry-service (consumer)

```yaml
spring.kafka.bootstrap-servers: ${SPRING_KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
spring.kafka.consumer.group-id: telemetry-service
```

Слушатели: `SatelliteLifecycleKafkaListener` (`@KafkaListener` на оба топика).

Проверка реестра: `GET http://localhost:8084/api/telemetry/known-satellites`

## Docker Compose

Сервис **kafka** (Apache Kafka, KRaft, порт **9092**).  
Переменная для **server** и **telemetry-service**:

```text
SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:9092
```

## Локальный запуск

1. `docker compose up kafka` (или весь стек)
2. `./gradlew :telemetry-service:bootRun`
3. `./gradlew :server:bootRun`

После старта server (инициализация «RU Basic») в логах telemetry появятся события CREATED; в `known-satellites` — имена спутников.
