# Запуск в Docker: центр управления, сервис миссий и телеметрия

Проект — Gradle-мультимодульный репозиторий с тремя Spring Boot-приложениями и общим модулем контрактов gRPC:

| Модуль | Роль | Порт по умолчанию | JAR |
|--------|------|-------------------|-----|
| **server** | REST API центра управления (спутники, группировки), Swagger UI, gRPC-клиент телеметрии | 8082 (HTTP) | `server.jar` |
| **mission-service** | Тонкий клиент: обращается к центру по базовому URL из окружения | 8083 | `mission-service.jar` |
| **telemetry-service** | gRPC-сервер потоковой телеметрии (температуры спутников) | 8084 (HTTP), **9091** (gRPC) | `telemetry-service.jar` |
| **telemetry-proto** | Единый `telemetry.proto` и сгенерированные Java/gRPC-классы (библиотека, не приложение) | — | — |
| **satellite-events** | JSON-события спутников для Kafka (библиотека) | — | — |
| **kafka** (Compose) | Брокер сообщений | **9092** | — |

Адрес центра **не зашивается в коде** `mission-service`: используется свойство `app.server.base-url`, в Docker задаётся **`SERVER_URL`** (например `http://server:8082`). Имя хоста `server` — имя сервиса в сети Docker Compose.

Центр подключается к телеметрии по gRPC через **`TELEMETRY_GRPC_ADDRESS`** (в compose: `static://telemetry-service:9091`).

---

## Требования

- JDK 21 (для локальной сборки без Docker)
- Docker Engine и Docker Compose v2

---

## Быстрый старт: сборка и запуск

Из корня репозитория:

```bash
docker compose up --build
```

Compose поднимает **PostgreSQL**, **Kafka** и три Spring Boot-приложения в сети **`space-net`**:

- **postgres** — PostgreSQL 16, БД `satellite_db`, пользователь `satellite`, том `postgres-data`, healthcheck `pg_isready`
- **kafka** — Apache Kafka (KRaft, `apache/kafka:latest`), `9092:9092`, `SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:9092` у server и telemetry-service
- **telemetry-service** — `8084:8084`, `9091:9091`, подписка на `satellite.created` / `satellite.deleted`, старт **после** healthy Kafka
- **server** — `8082:8082`, `SPRING_DATASOURCE_*` → `postgres`, публикация событий спутников в Kafka, `TELEMETRY_GRPC_ADDRESS` → `telemetry-service:9091`, старт **после** healthy Postgres и Kafka
- **mission-service** — `8083:8083`, `SERVER_URL=http://server:8082`, старт **после** healthy сервера

Образы помечены тегами версии **`1.0.0`** (см. `image:` в `docker-compose.yml`). Дополнительно можно пометить образ по коммиту:

```bash
docker tag satellite-server:1.0.0 satellite-server:$(git rev-parse --short HEAD)
```

---

## Проверка работы

### 1. Swagger UI центра управления (с ноутбука)

- [http://localhost:8082/swagger-ui/index.html](http://localhost:8082/swagger-ui/index.html)

(если редирект настроен иначе: [http://localhost:8082/swagger-ui.html](http://localhost:8082/swagger-ui.html))

### 2. Health endpoints

- Сервер: `GET http://localhost:8082/actuator/health`
- Сервис миссий: `GET http://localhost:8083/actuator/health`
- Телеметрия: `GET http://localhost:8084/actuator/health`

### 3. Запрос к API сервера (Postman / curl)

Обзор группировок:

```http
GET http://localhost:8082/api/overview
```

Список группировок:

```http
GET http://localhost:8082/api/constellations
```

### 4. Сервис миссий как прокси к центру

```http
GET http://localhost:8083/api/remote/overview
```

При успешном старте в логах `mission-service` будет сообщение о связи с центром (после `depends_on` и healthcheck сервера).

### 5. Телеметрия

Сервис **telemetry-service** отдаёт поток обновлений по gRPC; **server** подписывается при старте и записывает температуры в БД. Отдельного публичного REST API у телеметрии нет — проверка через health (п. 2) и логи `server` (`Telemetry gRPC client started`).

---

## Локальный запуск без Docker

Нужен **PostgreSQL** (см. `server/src/main/resources/application.yml`: по умолчанию `jdbc:postgresql://localhost:5432/satellite_db`, пользователь `satellite`).

Из корня:

```bash
# Терминал 1 — телеметрия (gRPC 9091)
./gradlew :telemetry-service:bootRun

# Терминал 2 — центр (ожидает telemetry на localhost:9091 по умолчанию)
./gradlew :server:bootRun

# Терминал 3 — миссии (центр на localhost:8082)
./gradlew :mission-service:bootRun
```

Порты: сервер `8082`, миссии `8083`, телеметрия HTTP `8084` / gRPC `9091`.

Для миссий при другом адресе центра (PowerShell):

```powershell
$env:SERVER_URL="http://127.0.0.1:8082"
./gradlew :mission-service:bootRun
```

Для центра при телеметрии на другом хосте:

```powershell
$env:TELEMETRY_GRPC_ADDRESS="static://127.0.0.1:9091"
./gradlew :server:bootRun
```

---

## Устройство Docker-файлов

### Многостадийная сборка

- **Стадия build**: `eclipse-temurin:21-jdk`, копируются `gradlew`, `settings.gradle`, `build.gradle`, модули `telemetry-proto`, `server`, `mission-service`, `telemetry-service`, выполняется `./gradlew :<module>:bootJar -x test`.
- **Стадия run**: `eclipse-temurin:21-jre-alpine`, только готовый JAR.

### Безопасность

- Процесс от пользователя `app`.
- В финальный слой добавлен `curl` для healthcheck.

### Файлы

- `server/Dockerfile` — центр управления
- `mission-service/Dockerfile` — сервис миссий
- `telemetry-service/Dockerfile` — телеметрия
- `docker-compose.yml` — сеть `space-net`, порты, переменные, healthcheck
- `.dockerignore` — исключает `build`, `.git` и лишнее из контекста

Контракт gRPC: **`telemetry-proto/src/main/proto/telemetry.proto`** (один источник для `server` и `telemetry-service`).

---

## Переменные окружения

| Переменная | Где используется | Назначение |
|------------|------------------|------------|
| `SERVER_PORT` | все три приложения | HTTP-порт Tomcat |
| `SERVER_URL` | `mission-service` | Базовый URL центра → `app.server.base-url` |
| `TELEMETRY_GRPC_ADDRESS` | `server` | Адрес gRPC телеметрии (compose: `static://telemetry-service:9091`) |
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | `server`, `telemetry-service` | Адрес Kafka (compose: `kafka:9092`) |
| `APP_KAFKA_ENABLED` | `server` | Отключить публикацию в Kafka (`false` в тестах) |
| `SPRING_DATASOURCE_URL` | `server` | JDBC URL PostgreSQL |
| `SPRING_DATASOURCE_USERNAME` | `server` | пользователь БД |
| `SPRING_DATASOURCE_PASSWORD` | `server` | пароль БД |
| `APP_CONSOLE_MENU_ENABLED` | `server` | консольное меню (`false` в Docker) |

---

## Сеть Docker

В `docker-compose.yml` — сеть **`space-net`** (`bridge`). Сервисы видят друг друга по имени: `postgres`, `server`, `mission-service`, `telemetry-service`.

---

## Сборка JAR без Docker

```bash
./gradlew :server:bootJar :mission-service:bootJar :telemetry-service:bootJar
```

Артефакты: `server/build/libs/server.jar`, `mission-service/build/libs/mission-service.jar`, `telemetry-service/build/libs/telemetry-service.jar`.

---

## Тесты

```bash
./gradlew :telemetry-proto:build :server:test :mission-service:test :telemetry-service:test
```
