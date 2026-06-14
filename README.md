# Satellite Manager

Gradle-мультимодульный проект: центр управления спутниковыми группировками, сервис миссий и потоковая телеметрия по gRPC.

## Быстрый старт (Docker)

**Требования:** Docker Engine, Docker Compose v2.

Из корня репозитория:

```bash
docker compose up --build
```

Поднимаются PostgreSQL, Redis, Kafka и три Spring Boot-приложения в сети `space-net`.

| Сервис | Порт | Назначение |
|--------|------|------------|
| **server** | 8082 | REST API, Swagger UI |
| **mission-service** | 8083 | Прокси к центру управления |
| **telemetry-service** | 8084 (HTTP), 9091 (gRPC) | Поток телеметрии |
| **postgres** | — | БД `satellite_db` |
| **redis** | — | Кэш чтения для `server` |
| **kafka** | 9092 | События жизненного цикла спутника |

Проверка:

- Swagger: [http://localhost:8082/swagger-ui/index.html](http://localhost:8082/swagger-ui/index.html)
- Health: `GET /actuator/health` на портах 8082, 8083, 8084

Подробнее о Docker, переменных окружения и локальном запуске без контейнеров — в [docs/AdditionalREADME/README-DOCKER.md](docs/AdditionalREADME/README-DOCKER.md).

## Структура проекта

```
satellite-manager/
├── server/                 # Центр управления (REST, JPA, Kafka producer, gRPC-клиент)
├── mission-service/        # Клиент к server по SERVER_URL
├── telemetry-service/      # gRPC-сервер телеметрии, Kafka consumer
├── telemetry-proto/        # Общий контракт gRPC (telemetry.proto)
├── satellite-events/       # JSON-модели событий для Kafka
├── docker-compose.yml
└── docs/                   # Дополнительная документация
```

## Основные зависимости

| Технология | Версия / примечание |
|------------|---------------------|
| Java | 21 |
| Spring Boot | 3.4.x |
| PostgreSQL | 16 (в Docker) |
| Redis | 7 (в Docker, кэш `server`) |
| Apache Kafka | KRaft (в Docker) |
| gRPC | `telemetry-proto`, grpc-spring-boot-starter |
| Flyway | Миграции БД |
| Gradle | Сборка всех модулей |

## Outbox и Inbox (Kafka)

События создания и удаления спутника (`satellite.created`, `satellite.deleted`) передаются через Kafka с гарантией согласованности:

- **Outbox (server)** — событие пишется в таблицу `outbox` в той же транзакции, что и изменение спутника; фоновый relay отправляет записи в Kafka.
- **Inbox (telemetry-service)** — входящие сообщения фиксируются в таблице `inbox` по `eventId`; повторная доставка не дублирует обработку.

Формат сообщений и конфигурация — в [docs/KAFKA_SATELLITE_EVENTS.md](docs/KAFKA_SATELLITE_EVENTS.md).

## Кэширование (Redis, server)

В модуле `server` включено кэширование чтения через **Spring Cache** и **Redis** (`@EnableCaching`, `spring-boot-starter-cache`, `spring-boot-starter-data-redis`).

Кэшируются методы сервисного слоя (`CrudManagementService`), не контроллеры:

| Метод | Ключ в Redis | TTL |
|-------|----------------|-----|
| `getSatellite(id)` | `satellite::{id}` | 10 мин |
| `getConstellation(name)` | `constellation::{name}` | 15 мин |
| `getAllSatellites()` | `satellites::all` | 5 мин |

Инвалидация (`@CacheEvict`):

- создание спутника — очищается `satellites::all`;
- обновление спутника (battery, state, телеметрия) — очищается `satellite::{id}`;
- удаление спутника — `satellite::{id}` и `satellites::all`;
- изменение состава группировки — `constellation::{name}` и `satellites::all`.

При недоступности Redis приложение продолжает работать без кэша (graceful degradation через `CacheErrorHandler`).

Проверка:

- `GET /api/crud/satellites` — список всех спутников (кэшируется);
- `GET /actuator/caches` — зарегистрированные кэши;
- `GET /actuator/metrics/cache.gets` — метрики кэша;
- `docker exec satellite-redis redis-cli KEYS '*'` — ключи в Redis.

Переменные окружения для `server`: `SPRING_DATA_REDIS_HOST`, `SPRING_DATA_REDIS_PORT` (в Docker Compose уже заданы).

## Сборка без Docker

```bash
./gradlew :server:bootJar :mission-service:bootJar :telemetry-service:bootJar
```

Тесты:

```bash
./gradlew test
```
