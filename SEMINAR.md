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
  `src/test/resources/application-test.properties`); `SatelliteServiceTest` replaces `Main` with a `@MockBean` so the
  console menu does not run.

## How to run

```bash
./gradlew test
./gradlew bootRun
```

## Known issues

- `@MockBean` is deprecated in Spring Boot 3.4 (still works).
- `@EqualsAndHashCode` on mutable satellites can change equality while an object sits in a collection.
- "Emergency charging" in the special operations menu only prints that the battery level was raised to 50%; it does
  not change the battery level (there is no charging API in `EnergySystem` yet).
- The `test.mode` property only skips the "press Enter" prompt after initialization; it does not stop the menu.

## Fixed after the seminar

- The menu asked to "press Enter" before initialization as well as after it; only the prompt after initialization
  is left.
- On exit the repository contents were printed twice; they are now printed once.
- `gradle-wrapper.jar` was missing from Git; it is now tracked (done in the commit that added this file).
- The tracked `gradle-wrapper.jar` came from an older Gradle and had no `Main-Class`, while `gradlew` (8.14) runs it
  with `-jar`, so `./gradlew` failed with "no main manifest attribute"; the jar was replaced with the Gradle 8.14 one.
- `bootRun` did not forward the terminal input, so the console menu failed with "No line found"; `bootRun` and `run`
  now set `standardInput = System.in`.
- `./gradlew run` failed with "No main class specified"; `application { mainClass = 'com.satellite.app.Main' }` was added.
- `SatelliteParam.toString()` printed the 0..1 battery level as a percentage (`0.9%`); it is now multiplied by 100.
- `src/Satellite_constellation.iml` (an IntelliJ module file) was still tracked; it was removed from Git.
- `Satellite.activate()` printed "low battery" for a satellite that was already active; it now says that the
  satellite is already active.
- "Activate by type" counted every satellite of the type, even those that failed to activate; it now counts only
  satellites that were actually activated.
- In the "add satellite" menu, a battery level outside 0..1 made the energy system builder throw and stopped the
  application; the menu now asks for the value again.
- The "integration" test for `ConstellationRepository` used `MockitoExtension` / `@InjectMocks` and did not start the
  Spring context. It now uses `@SpringBootTest` with the `@Autowired` repository and service (as in `seminar_5`).
  `Main` got the property `satellite.console.enabled` (default `true`); the test sets it to `false` so the console
  menu does not run.
- Comments in `application.properties` had broken encoding (`????`); rewritten in English.
- All text (docs, comments, messages, test names) translated to English.
