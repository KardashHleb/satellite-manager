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
and remove boilerplate with **Lombok**. The assignment text (in Russian) is in [`README.md`](README.md).

## What was done

- `build.gradle`: `jacoco` plugin, `spring-boot-starter-test`, Lombok (main and test). `test` is
  `finalizedBy jacocoTestReport`, so every test run produces a coverage report.
- Lombok on the domain classes: `@Getter`, `@ToString`, `@EqualsAndHashCode` on the satellites and `EnergySystem`;
  `@Getter/@Setter` and constructors on `SatelliteState`.
- Tests in `src/test/java/com/satellite/app/`:
  - `repository/ConstellationRepositoryUnitTest` — plain unit tests of the in-memory repository;
  - `repository/ConstellationRepositoryMockTest` — Mockito, replaces the internal map via reflection;
  - `repository/ConstellationRepositoryIntegrationTest`;
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

- The "integration" test uses `MockitoExtension` / `@InjectMocks` rather than `@SpringBootTest`, so it does not
  start the Spring context.
- `@EqualsAndHashCode` on mutable satellites can change equality while an object sits in a collection.
- The Gradle cache directory `.gradle/` is still tracked (removed in seminar 5).
- `gradle-wrapper.jar` was missing from Git; **fixed in the commit that added this file**.
