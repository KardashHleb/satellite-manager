# Seminar 11 — Load testing with k6 and Allure

| | |
|---|---|
| Branch | `seminar_11_performance_testing` |
| Based on | [`seminar_11_fix`](https://github.com/KardashHleb/satellite-manager/tree/seminar_11_fix) |
| Last commit | 2026-06-06 |
| Next | [`seminar_12`](https://github.com/KardashHleb/satellite-manager/tree/seminar_12) |
| All seminars | [SEMINARS.md on main](https://github.com/KardashHleb/satellite-manager/blob/main/SEMINARS.md) |

## Learning goal

Measure how the API behaves **under load**: script a realistic user flow in **k6**, define pass/fail
**thresholds**, and publish the results as an **Allure** report.

## What was done

- **`load-tests/scenario.js`** — an operator flow against `BASE_URL` (default `http://localhost:8082`):
  overview → read the demo constellation → create a constellation → add a satellite → rename the constellation →
  delete the satellite → delete the constellation. Every virtual user works with its own constellation
  (`k6-c-{VU}-{ITER}`), so users do not interfere with each other.
- **Load profiles and thresholds**:

  | Profile | Load | Thresholds |
  |---|---|---|
  | `smoke1` | 1 virtual user, 30 s | failed requests < 1% |
  | `smoke2` | 2 virtual users, 30 s | failed requests < 1% |
  | `load10` | ramp up to 10 users, hold 60 s | failed requests < 5%, p95 latency < 3000 ms |

- **Reporting**: `load-tests/lib/allure-summary.js` turns the k6 summary into Allure results (one step per scenario
  group plus a metrics attachment); `package.json` provides the scripts and `allure-commandline`.
- **Docs**: security reports moved to `docs/securityReports/`, extra guides to `docs/AdditionalREADME/`
  (Docker, security, draft); the README documents the scenario.

## How to run

Start the system, then run k6 (requires k6 and Node.js):

```bash
docker compose up --build
cd load-tests
npm install
npm run test:1      # smoke, 1 user
npm run test:10     # load, 10 users
npm run report      # build the Allure report
npm run serve       # open it
```

Against another host: `k6 run -e BASE_URL=http://host:8082 -e PROFILE=load10 scenario.js`.

## Known issues

- The Allure status is "failed" as soon as any request fails, even if the k6 thresholds pass.
- The SBOM in `docs/securityReports/` was generated from the source folder and lists no Gradle dependencies, so the
  clean Grype result is not meaningful; scanning the built images or jars gives a real picture.
- Kafka advertises only `kafka:9092`, so the server and telemetry service started on the host with `bootRun`
  (default `localhost:9092`) cannot use the Kafka from `docker compose up kafka`; run the whole stack in Compose or add a
  host listener.
- The console menu item "Emergency satellite charging" only prints which satellites would be charged; it does not change the battery level.

## Fixed after the seminar

- `gradle-wrapper.jar` was missing from Git, which broke `./gradlew` and the Docker build on a fresh clone; it was added in the commit that added this file.
- Satellite operations (activate, deactivate, photo, send data, single-satellite mission) changed a detached copy, so with `open-in-view: false` nothing was saved; `ConstellationService` now changes the managed instance (test: `SatelliteOperationsPersistenceTest`).
- `SpaceOperationController` answered every error with 400 and emoji strings; it now returns 404 for an unknown constellation or satellite, 400 for bad input, 500 otherwise, without emojis.
- The console "test data transmission" and "test imaging" called the satellite directly and their result was not saved; they now go through the service.
- `Satellite.activate()` on an already active satellite printed "battery level is low", and the console "activate by type" counted satellites that failed to activate; it now reports "already active" and counts only satellites that were turned on (test: `SatelliteActivationTest`).
- `./gradlew :server:bootRun` did not pass the console input to the application, so the console menu stopped at the first prompt; the `JavaExec` tasks now use `standardInput = System.in`.
- `docs/KAFKA_SATELLITE_EVENTS.md` named the wrong Compose variable for topic auto-creation; it now names `KAFKA_AUTO_CREATE_TOPICS_ENABLE`.
- Added a test for the fix made in seminar 11 (API automation): `SatelliteEventPublishingTest` (the CREATED event carries the database id) and the id check in `SatelliteOperationsPersistenceTest`.
- All text (docs, comments, messages, test names) translated to English. The demo satellites are now `Comm-1`, `Comm-2`, `Imager-1` ... The generated security reports in `docs/securityReports/` are left unchanged. The two comments in the Flyway migration `V1__init_schema.sql` are kept in the original language on purpose: editing an applied migration changes its checksum, and Flyway would refuse to start on an existing database.
