
Мультимодульный Gradle-проект:

- **server** — центр управления (REST, Swagger, JPA + PostgreSQL, gRPC-клиент телеметрии);
- **mission-service** — клиент к центру по `SERVER_URL`;
- **telemetry-service** — gRPC-поток телеметрии;
- **telemetry-proto** — общий контракт `telemetry.proto` для server и telemetry-service;
- **satellite-events** — JSON-события спутников для Kafka.

События создания/удаления спутника (Kafka): [docs/KAFKA_SATELLITE_EVENTS.md](docs/KAFKA_SATELLITE_EVENTS.md).

Подробные инструкции по Docker, переменным окружения и проверке работы — в [README-DOCKER.md](README-DOCKER.md).

Порты по умолчанию: центр — **8082**, миссии — **8083**, телеметрия HTTP — **8084**, gRPC — **9091**.

**База данных:** локально поднимите PostgreSQL (например `localhost:5432`, БД `satellite_db`, пользователь/пароль `satellite`/`satellite` — как в `application.yml`) или используйте `docker compose up` (в compose добавлен сервис `postgres`). Схема создаётся **Flyway** (`server/src/main/resources/db/migration`), Hibernate в режиме `ddl-auto: validate`.

Вопрос из задания про **@Embedded** и **@OneToOne** — в [docs/JPA_EMBEDDED_VS_ONETOONE.md](docs/JPA_EMBEDDED_VS_ONETOONE.md).
