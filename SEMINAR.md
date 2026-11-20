# Seminar 9.2 — JPA, PostgreSQL, Flyway and CRUD API

| | |
|---|---|
| Branch | `seminar_9.2` |
| Based on | [`seminar_9`](https://github.com/KardashHleb/satellite-manager/tree/seminar_9) |
| Last commit | 2026-04-26 |
| Next | [`seminar_10`](https://github.com/KardashHleb/satellite-manager/tree/seminar_10) (gRPC), [`seminar_11`](https://github.com/KardashHleb/satellite-manager/tree/seminar_11) (Kafka) |
| All seminars | [SEMINARS.md on main](https://github.com/KardashHleb/satellite-manager/blob/main/SEMINARS.md) |

## Learning goal

Replace the in-memory storage with a real **relational database**: map the domain with **JPA / Hibernate**,
manage the schema with **Flyway** migrations, run **PostgreSQL** in Docker, and expose a full **CRUD** API.

## What was done

- **Entities**: `Satellite` with `JOINED` inheritance into `CommunicationSatellite` and `ImagingSatellite`;
  `SatelliteConstellation`; `EnergySystem` and `SatelliteState` as separate entities linked with `@OneToOne`.
- **Repositories**: `ConstellationRepository` rewritten on Spring Data JPA; new `SatelliteRepository`,
  `EnergySystemRepository`, `SatelliteStateRepository`.
- **`CrudManagementService`** and CRUD controllers (`controller/crud/`):

  | Resource | Operations |
  |---|---|
  | `/api/crud/constellations` | GET list, GET `/{name}`, POST, PUT `/{name}` (rename), DELETE `/{name}` |
  | `/api/crud/satellites` | GET `/by-constellation/{c}`, GET `/{id}`, POST, DELETE `/{id}` |
  | `/api/crud/energy-systems/{id}` | GET, PATCH (battery level) |
  | `/api/crud/satellite-states/{id}` | GET, PATCH (active flag) |

- **Schema**: `application.yml` with `ddl-auto: validate`; migration `db/migration/V1__init_schema.sql`.
- **Compose**: `postgres:16-alpine` with a named volume; the server depends on it.
- `test.html` became a small UI for both services; CORS enabled in `mission-service`.
- **Tests**: `@DataJpaTest` repository tests on H2 (`*RepositoryJpaTest`, `application-test.yml`).
- **`docs/JPA_EMBEDDED_VS_ONETOONE.md`** answers the seminar question: `@Embedded` vs `@OneToOne` for the energy
  system and state, and why separate tables were chosen.

## How to run

```bash
docker compose up --build
```

Without Docker you need PostgreSQL at `localhost:5432/satellite_db` (user and password `satellite`), then:

```bash
./gradlew :server:bootRun
```

## Known issues

- `gradle-wrapper.jar` was missing from Git, which also broke the Docker build; **fixed in the commit that added this file**.
