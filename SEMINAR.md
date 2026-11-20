# Seminar 5 — GoF patterns, part 1: Builder and Factory Method

| | |
|---|---|
| Branch | `seminar_5` |
| Based on | [`seminar_3`](https://github.com/KardashHleb/satellite-manager/tree/seminar_3), includes the testing commit of [`seminar_4`](https://github.com/KardashHleb/satellite-manager/tree/seminar_4) |
| Last commit | 2026-02-17 |
| Next | [`seminar_6`](https://github.com/KardashHleb/satellite-manager/tree/seminar_6) |
| All seminars | [SEMINARS.md on main](https://github.com/KardashHleb/satellite-manager/blob/main/SEMINARS.md) |

## Learning goal

Apply the creational **Gang of Four** patterns to the domain model: **Builder** for a validated value object and
**Factory Method** for creating satellites of different types.

## What was done

- Everything from seminar 4: tests, JaCoCo and Lombok.
- **Builder** — `EnergySystem` has a private constructor, a static `builder()` and an inner `EnergySystemBuilder`.
  The builder validates the battery level (0..1) and throws instead of silently clamping. `Satellite` creates its
  energy system through the builder.
- **Factory Method** — `factory/SatelliteFactory` (interface) with two Spring `@Component` implementations,
  `CommunicationSatelliteFactory` and `ImagingSatelliteFactory`. `Main` creates satellites only through factories.
- Repository hygiene: the accidentally committed `.gradle/` cache was removed and added to `.gitignore`.

## How to run

```bash
./gradlew test
./gradlew bootRun
```

## Known issues

- `@EqualsAndHashCode` on mutable satellites can change equality while an object sits in a collection.
- "Emergency charging" in the special operations menu only prints that the battery level was raised to 50%; it does
  not change the battery level (there is no charging API in `EnergySystem` yet).

## Fixed after the seminar

- Some factory shortcuts (`createWithBandwidth(name, bandwidth)`, `createWithResolution(name, resolution)`,
  `createSatellite(name)`) passed a battery level of `100.0`, which the builder rejects with
  `IllegalArgumentException`; the default is now `1.0` (full battery).
- In the "add satellite" menu, a battery level outside 0..1 made the builder throw and stopped the application;
  the menu now asks for the value again.
- The "integration" test used `MockitoExtension` / `@InjectMocks` and did not start the Spring context. It now uses
  `@SpringBootTest` with `@Autowired` repository and service and also covers the full lifecycle through the service.
  The console menu is switched off in tests with the property `satellite.console.enabled=false`.
- `gradle-wrapper.jar` was missing from Git; it is now tracked (done in the commit that added this file).
- The tracked `gradle-wrapper.jar` came from an older Gradle and had no `Main-Class`, while `gradlew` (8.14) runs it
  with `-jar`, so `./gradlew` failed with "no main manifest attribute"; the jar was regenerated for Gradle 8.14.
- `./gradlew run` failed with "No main class specified"; `application { mainClass = 'com.satellite.app.Main' }` was added.
- `bootRun` / `run` did not forward the terminal input, so the console menu got end-of-input and the app exited at
  once; both tasks now set `standardInput = System.in`.
- `src/Satellite_constellation.iml` (an IntelliJ module file) was still tracked; it was removed from Git.
- Comments in `application.properties` had broken encoding (`????`); rewritten in English.
- `Satellite.activate()` printed "low battery" for a satellite that was already active; it now says that the
  satellite is already active.
- "Activate by type" counted every satellite of the type, even those that failed to activate; it now counts only
  satellites that were actually activated.
- All text (docs, comments, messages, test names) translated to English.
