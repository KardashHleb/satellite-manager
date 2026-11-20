A multi-module Gradle project: the **server** module (mission control center, REST, Swagger, JPA + PostgreSQL) and **mission-service** (a client of the center, configured with `SERVER_URL`).

Detailed instructions on Docker, environment variables and how to check that everything works are in [README-DOCKER.md](README-DOCKER.md).

Default ports: center — **8082**, mission service — **8083** (see `server/src/main/resources/application.yml` and `mission-service/.../application.properties`).

**Database:** start PostgreSQL locally (for example `localhost:5432`, database `satellite_db`, user/password `satellite`/`satellite`, as in `application.yml`) or use `docker compose up` (the compose file includes a `postgres` service). The schema is created by **Flyway** (`server/src/main/resources/db/migration`), Hibernate runs with `ddl-auto: validate`.

The assignment question about **@Embedded** vs **@OneToOne** is answered in [docs/JPA_EMBEDDED_VS_ONETOONE.md](docs/JPA_EMBEDDED_VS_ONETOONE.md).
