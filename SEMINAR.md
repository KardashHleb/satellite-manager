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

- The console menu item "Emergency satellite charging" only prints which satellites would be charged; it does not change the battery level.

## Fixed after the seminar

- `gradle-wrapper.jar` was missing from Git (this broke `./gradlew` and the Docker build), and the jar added later had no `Main-Class`; it is now the Gradle 8.14 wrapper jar.
- Satellite operations (activate, deactivate, photo, send data, single-satellite mission) changed a detached copy, so with `open-in-view: false` nothing was saved; `ConstellationService` now changes the managed instance (test: `SatelliteOperationsPersistenceTest`).
- `POST /api/crud/satellites` returned the new satellite without its id; `CrudManagementService` now reads the saved satellite back.
- `SpaceOperationController` answered every error with 400 and emoji strings; it now returns 404 for an unknown constellation or satellite, 400 for bad input, 500 otherwise, without emojis.
- The console "test data transmission" and "test imaging" called the satellite directly and their result was not saved; they now go through the service.
- `Satellite.activate()` on an already active satellite printed "battery level is low", and the console "activate by type" counted satellites that failed to activate; it now reports "already active" and counts only satellites that were turned on.
- `./gradlew :server:bootRun` failed with "No line found" in the console menu; `bootRun` now forwards `System.in`.
- The console menu asked to press Enter before and after the start-up banner and printed the repository contents twice on exit; it now asks once and prints them once.
- The console "add a new satellite" menu accepted a battery level outside 0.0-1.0, which crashed with an exception; it now asks again.
- `SatelliteParam.toString()` printed the 0..1 battery level as a percentage (for example "0.8%"); it now prints "80%".
- All text (docs, comments, messages, test names) translated to English. The two comments in the Flyway migration `V1__init_schema.sql` are kept in the original language on purpose: editing an applied migration changes its checksum, and Flyway would refuse to start on an existing database.
