
Мультимодульный Gradle-проект: модуль **server** (центр управления, REST, Swagger, JPA + PostgreSQL) и **mission-service** (клиент к центру по `SERVER_URL`).

Подробные инструкции по Docker, переменным окружения и проверке работы — в [README-DOCKER.md](README-DOCKER.md).

Порты по умолчанию: центр — **8082**, сервис миссий — **8083** (см. `server/src/main/resources/application.yml` и `mission-service/.../application.properties`).

**База данных:** локально поднимите PostgreSQL (например `localhost:5432`, БД `satellite_db`, пользователь/пароль `satellite`/`satellite` — как в `application.yml`) или используйте `docker compose up` (в compose добавлен сервис `postgres`). Схема создаётся **Flyway** (`server/src/main/resources/db/migration`), Hibernate в режиме `ddl-auto: validate`.

Вопрос из задания про **@Embedded** и **@OneToOne** — в [docs/JPA_EMBEDDED_VS_ONETOONE.md](docs/JPA_EMBEDDED_VS_ONETOONE.md).
