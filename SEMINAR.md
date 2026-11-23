# Seminar 11 (extra) — Integration with an API test-automation project

| | |
|---|---|
| Branch | `seminar_11_api_automation` |
| Based on | [`seminar_11`](https://github.com/KardashHleb/satellite-manager/tree/seminar_11) |
| Last commit | 2026-05-24 |
| Next | [`seminar_11_devSecAudit`](https://github.com/KardashHleb/satellite-manager/tree/seminar_11_devSecAudit) continues the main line |
| All seminars | [SEMINARS.md on main](https://github.com/KardashHleb/satellite-manager/blob/main/SEMINARS.md) |

## Learning goal

Prepare the system to be tested by a **separate automated API test suite** (Gradle + Allure reports), and fix the
API behavior that those tests exposed.

## What was done

- **README**: how to run the stack and then the external `api-automation` project against it
  (`./gradlew clean test`, then `allureServe` or `allureReport`); base URLs 8082, 8083, 8084.
- **Fixes found by the automated tests**:
  - `ConstellationService.addSatellite` now uses `saveAndFlush` and reads the saved satellite back, so the Kafka
    event carries the real database id instead of `null`;
  - `CrudManagementService.addSatelliteFromRequest` returns the saved entity (and a 500 error if it cannot be found);
  - `ConstellationServiceTest` updated accordingly.

## How to run

```bash
docker compose up --build
```

Then, in the separate api-automation repository:

```bash
./gradlew clean test
./gradlew allureServe
```

## Known issues

- The automated tests live in another repository and are not included here.
- Kafka advertises only `kafka:9092`, so the server and telemetry service started on the host with `bootRun`
  (default `localhost:9092`) cannot use the Kafka from `docker compose up kafka`; run the whole stack in Compose or add a
  host listener.
- The console menu item "Emergency satellite charging" only prints which satellites would be charged; it does not change the battery level.

## Fixed after the seminar

- `gradle-wrapper.jar` was missing from Git (this broke `./gradlew` and the Docker build), and the jar added later had no `Main-Class`; it is now the Gradle 8.14 wrapper jar.
- Satellite operations (activate, deactivate, photo, send data, single-satellite mission) changed a detached copy, so with `open-in-view: false` nothing was saved; `ConstellationService` now changes the managed instance (test: `SatelliteOperationsPersistenceTest`).
- `SpaceOperationController` answered every error with 400 and emoji strings; it now returns 404 for an unknown constellation or satellite, 400 for bad input, 500 otherwise, without emojis.
- The console "test data transmission" and "test imaging" called the satellite directly and their result was not saved; they now go through the service.
- `Satellite.activate()` on an already active satellite printed "battery level is low", and the console "activate by type" counted satellites that failed to activate; it now reports "already active" and counts only satellites that were turned on.
- `./gradlew :server:bootRun` did not pass the console input to the application, so the console menu stopped at the first prompt; the `JavaExec` tasks now use `standardInput = System.in`.
- `docs/KAFKA_SATELLITE_EVENTS.md` named the wrong Compose variable for topic auto-creation; it now names `KAFKA_AUTO_CREATE_TOPICS_ENABLE`.
- Added tests for the fixes made in this seminar: `SatelliteEventPublishingTest` (the CREATED event carries the database id) and the id check in `SatelliteOperationsPersistenceTest`.
- All text (docs, comments, messages, test names) translated to English. The demo satellites are now `Comm-1`, `Comm-2`, `Imager-1` ... The two comments in the Flyway migration `V1__init_schema.sql` are kept in the original language on purpose: editing an applied migration changes its checksum, and Flyway would refuse to start on an existing database.
