# Запуск в Docker: центр управления и сервис миссий

Проект разделён на два Spring Boot-приложения в одном Gradle-мультимодульном репозитории:

| Модуль | Роль | Порт по умолчанию | JAR |
|--------|------|-------------------|-----|
| **server** | REST API центра управления (спутники, группировки), Swagger UI | 8082 | `server.jar` |
| **mission-service** | Тонкий клиент: обращается к центру по базовому URL из окружения | 8083 | `mission-service.jar` |

Адрес центра **не зашивается в коде**: в `mission-service` используется свойство `app.server.base-url`, которое в Docker задаётся через переменную окружения **`SERVER_URL`** (например `http://server:8082`). Имя хоста `server` — это имя контейнера в пользовательской сети Docker Compose; встроенный DNS резолвит его в IP контейнера.

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

Compose поднимает два **отдельных** сервиса (два контейнера) в общей сети `satellite-net`:

- **server** — проброс порта `8082:8082`, переменная `SERVER_PORT=8082`, healthcheck по `GET /actuator/health`
- **mission-service** — `8083:8083`, `SERVER_PORT=8083`, `SERVER_URL=http://server:8082`, старт **после** того, как сервер станет healthy (`depends_on: condition: service_healthy`)

Образы помечены тегами версии **`1.0.0`** (см. `image:` в `docker-compose.yml`). Дополнительно можно пометить образ по коммиту, например:

```bash
docker tag satellite-server:1.0.0 satellite-server:$(git rev-parse --short HEAD)
```

---

## Проверка работы

### 1. Swagger UI центра управления (с ноутбука)

Откройте в браузере:

- [http://localhost:8082/swagger-ui/index.html](http://localhost:8082/swagger-ui/index.html)

(если редирект настроен иначе, попробуйте [http://localhost:8082/swagger-ui.html](http://localhost:8082/swagger-ui.html))

### 2. Health endpoints

- Сервер: `GET http://localhost:8082/actuator/health`
- Сервис миссий: `GET http://localhost:8083/actuator/health`

### 3. Запрос к API сервера (Postman / curl)

Пример обзора группировок:

```http
GET http://localhost:8082/api/overview
```

Пример списка группировок:

```http
GET http://localhost:8082/api/constellations
```

### 4. Сервис миссий как прокси к центру

Сервис миссий дергает центр по внутреннему URL и отдаёт тот же текст обзора:

```http
GET http://localhost:8083/api/remote/overview
```

При успешном старте в логах `mission-service` будет сообщение о том, что связь с `/api/overview` установлена (после того как сервер уже поднят благодаря `depends_on` и healthcheck).

---

## Локальный запуск без Docker

Из корня:

```bash
# Терминал 1 — центр
./gradlew :server:bootRun

# Терминал 2 — миссии (по умолчанию ждёт центр на localhost:8082)
./gradlew :mission-service:bootRun
```

Порты по умолчанию: сервер `8082`, миссии `8083`. Для миссий при другом адресе центра:

```bash
set SERVER_URL=http://127.0.0.1:8082
./gradlew :mission-service:bootRun
```

(В PowerShell: `$env:SERVER_URL="http://127.0.0.1:8082"`)

---

## Устройство Docker-файлов

### Многостадийная сборка

- **Стадия build**: `eclipse-temurin:21-jdk-alpine`, в контекст копируются `gradlew`, `settings.gradle`, `build.gradle`, каталоги `server` и `mission-service`, выполняется `./gradlew :<module>:bootJar -x test`.
- **Стадия run**: `eclipse-temurin:21-jre-alpine`, в образ попадает только готовый JAR (`server.jar` или `mission-service.jar`), без исходников и без каталогов сборки целиком.

### Безопасность

- Процесс в контейнере запускается от непривилегированного пользователя `app` (группа `app`).
- В финальный слой добавлен `curl` для healthcheck и отладки.

### Файлы

- `server/Dockerfile` — образ центра управления
- `mission-service/Dockerfile` — образ сервиса миссий
- `docker-compose.yml` — сеть, порты, переменные окружения, healthcheck сервера и ожидание готовности миссиями
- `.dockerignore` — исключает `build`, `.git` и лишнее из контекста сборки

---

## Переменные окружения

| Переменная | Где используется | Назначение |
|------------|------------------|------------|
| `SERVER_PORT` | оба приложения | Порт встроенного HTTP-сервера Tomcat |
| `SERVER_URL` | только `mission-service` | Базовый URL центра (`http://<имя_хоста>:<порт>`), подставляется в `app.server.base-url` |

В коде `mission-service` базовый URL читается только из конфигурации Spring (`@Value("${app.server.base-url}")`), которая привязана к `SERVER_URL` через `application.properties`.

---

## Сеть Docker

В `docker-compose.yml` объявлена пользовательская сеть `satellite-net` (тип `bridge`). Сервисы подключаются к ней явно; контейнеры видят друг друга по **имени сервиса** (`server`, `mission-service`), что и используется в `SERVER_URL=http://server:8082`.

---

## Сборка JAR без Docker

```bash
./gradlew :server:bootJar :mission-service:bootJar
```

Артефакты: `server/build/libs/server.jar`, `mission-service/build/libs/mission-service.jar`.

---

## Тесты

```bash
./gradlew :server:test :mission-service:test
```
