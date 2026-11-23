# Seminar 11 — Fixes after the security scan

| | |
|---|---|
| Branch | `seminar_11_fix` |
| Based on | [`seminar_11_devSecAudit`](https://github.com/KardashHleb/satellite-manager/tree/seminar_11_devSecAudit) |
| Last commit | 2026-05-31 |
| Next | [`seminar_11_performance_testing`](https://github.com/KardashHleb/satellite-manager/tree/seminar_11_performance_testing) |
| All seminars | [SEMINARS.md on main](https://github.com/KardashHleb/satellite-manager/blob/main/SEMINARS.md) |

## Learning goal

Close the loop of a security audit: **fix** what the scanners found, then **scan again** to confirm.

## What was done

- **Dependency update**: Spring Boot `3.4.2` → `3.4.13` in `build.gradle`, the usual remedy for known CVEs in
  framework and transitive dependencies.
- **Line endings**: `.gitattributes` forces LF for `gradlew` and `gradle/wrapper/**`, and all three Dockerfiles run
  `sed -i 's/\r$//' gradlew` before building. Without this, a Windows checkout produced a `gradlew` script that
  could not run inside Linux containers.
- **Re-scan**: `gitleaks-git-report2.json` (no secrets) and `semgrep-report2.json`.

## How to run

```bash
docker compose up --build
```

## Known issues

- The 15 warnings in `semgrep-report2.json` are all `plaintext-http-link` inside the committed ZAP HTML reports, not
  in the application code.
- The ZAP findings (actuator information, missing security headers) are not addressed yet.
- The SBOM was generated from the source folder and lists no Gradle dependencies, so the clean Grype result is
  not meaningful; scanning the built images or jars gives a real picture.
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
- All text (docs, comments, messages, test names) translated to English. The demo satellites are now `Comm-1`, `Comm-2`, `Imager-1` ... The generated security reports (`*-report*.json/html`, `sbom.json`) are left unchanged. The two comments in the Flyway migration `V1__init_schema.sql` are kept in the original language on purpose: editing an applied migration changes its checksum, and Flyway would refuse to start on an existing database.
