# Seminar 6 — GoF patterns, part 2: parameter objects and strategy-based factories

| | |
|---|---|
| Branch | `seminar_6` |
| Based on | [`seminar_5`](https://github.com/KardashHleb/satellite-manager/tree/seminar_5) |
| Last commit | 2026-02-28 |
| Next | [`seminar_7`](https://github.com/KardashHleb/satellite-manager/tree/seminar_7) |
| All seminars | [SEMINARS.md on main](https://github.com/KardashHleb/satellite-manager/blob/main/SEMINARS.md) |

## Learning goal

Make object creation **open for extension**: describe a satellite with a parameter object and let Spring pick the
right factory at runtime from all registered implementations (a **Strategy**-style dispatch over factories).

## What was done

- **Parameter objects** in `model/`: abstract `SatelliteParam` with `CommunicationSatelliteParam` and
  `ImagingSatelliteParam`, plus the enum `model/enums/SatelliteType { IMAGE, COMMUNICATION }`.
- **Factories** now implement `createSatelliteWithParameter(SatelliteParam)` and `isSatelliteTypeSupported(type)`.
- **`SatelliteService` / `SatelliteServiceImpl`** (`service/`): Spring injects `List<SatelliteFactory>`, and the
  service picks the first factory that supports the requested type. Adding a new satellite type means adding a new
  factory bean, without changing the service.
- **`SpaceOperationException`** (`exception/`) for domain errors.
- Tests: `factory/SatelliteFactoryTest`, `service/SatelliteServiceTest` (`@SpringBootTest`, `test` profile,
  `src/test/resources/application-test.properties`). A `test.mode` property keeps the console menu from blocking tests.

## How to run

```bash
./gradlew test
./gradlew bootRun
```

## Known issues

- `@MockBean` is deprecated in Spring Boot 3.4 (still works).
- The menu asks to "press Enter" before initialization as well as after it.
- `gradle-wrapper.jar` was missing from Git; **fixed in the commit that added this file**.
