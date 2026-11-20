# Seminar 4 — Unit, mock and integration tests, JaCoCo, Lombok

| | |
|---|---|
| Branch | `seminar_4` (side branch) |
| Based on | [`seminar_3`](https://github.com/KardashHleb/satellite-manager/tree/seminar_3) |
| Last commit | 2026-02-08 |
| Next | [`seminar_5`](https://github.com/KardashHleb/satellite-manager/tree/seminar_5) also contains this testing work |
| All seminars | [SEMINARS.md on main](https://github.com/KardashHleb/satellite-manager/blob/main/SEMINARS.md) |

## Learning goal

Learn the kinds of automated tests (**unit**, **mock**, **integration**), measure **code coverage** with JaCoCo,
and remove boilerplate with **Lombok**. The assignment text is in [`README.md`](README.md).

## What was done

- `build.gradle`: `jacoco` plugin, `spring-boot-starter-test`, Lombok (main and test). `test` is
  `finalizedBy jacocoTestReport`, so every test run produces a coverage report.
- Lombok on the domain classes: `@Getter`, `@ToString`, `@EqualsAndHashCode` on the satellites and `EnergySystem`;
  `@Getter/@Setter` and constructors on `SatelliteState`.
- Tests in `src/test/java/com/satellite/app/`:
  - `repository/ConstellationRepositoryUnitTest` — plain unit tests of the in-memory repository;
  - `repository/ConstellationRepositoryMockTest` — Mockito, replaces the internal map via reflection;
  - `repository/ConstellationRepositoryIntegrationTest` — `@SpringBootTest` with the injected repository and service;
  - `service/SpaceOperationCenterServiceTest` — service tested with a mocked repository.
- `Main`: sturdier input parsing; the `Scanner` is closed on exit.

## How to run

```bash
./gradlew test
```

Reports:

- tests: `build/reports/tests/test/index.html`
- coverage: `build/reports/jacoco/test/html/index.html`

## Known issues

- `@EqualsAndHashCode` on mutable satellites can change equality while an object sits in a collection.
- "Emergency charging" in the special operations menu only prints that the battery level was raised to 50%; it does
  not change the battery level (there is no charging API in `EnergySystem` yet).

## Fixed after the seminar

- The "integration" test used `MockitoExtension` / `@InjectMocks` and did not start the Spring context. It now uses
  `@SpringBootTest` with `@Autowired` repository and service and also covers the full lifecycle through the service.
  The console menu is switched off in tests with the property `satellite.console.enabled=false`.
- `gradle-wrapper.jar` was missing from Git; it is now tracked (done in the commit that added this file).
- The tracked `gradle-wrapper.jar` came from an older Gradle and had no `Main-Class`, while `gradlew` (8.14) runs it
  with `-jar`, so `./gradlew` failed with "no main manifest attribute"; the jar was regenerated for Gradle 8.14.
- `./gradlew run` failed with "No main class specified"; `application { mainClass = 'com.satellite.app.Main' }` was added.
- `bootRun` / `run` did not forward the terminal input, so the console menu got end-of-input and the app exited at
  once; both tasks now set `standardInput = System.in`.
- The Gradle cache directory `.gradle/` was tracked; it was removed from Git and added to `.gitignore`.
- `src/Satellite_constellation.iml` (an IntelliJ module file) was still tracked; it was removed from Git.
- Comments in `application.properties` had broken encoding (`????`); rewritten in English.
- `Satellite.activate()` printed "low battery" for a satellite that was already active; it now says that the
  satellite is already active.
- "Activate by type" counted every satellite of the type, even those that failed to activate; it now counts only
  satellites that were actually activated.
- All text (docs, comments, messages, test names) translated to English; the assignment text in `README.md` too.
