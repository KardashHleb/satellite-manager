# Seminar 7 — GoF patterns, part 3: Facade and AOP

| | |
|---|---|
| Branch | `seminar_7` |
| Based on | [`seminar_6`](https://github.com/KardashHleb/satellite-manager/tree/seminar_6) |
| Last commit | 2026-03-06 |
| Next | [`seminar_8`](https://github.com/KardashHleb/satellite-manager/tree/seminar_8) / [`seminar_9`](https://github.com/KardashHleb/satellite-manager/tree/seminar_9) (web), [`seminar_11_devSecAudit`](https://github.com/KardashHleb/satellite-manager/tree/seminar_11_devSecAudit) (main line) |
| All seminars | [SEMINARS.md on main](https://github.com/KardashHleb/satellite-manager/blob/main/SEMINARS.md) |

## Learning goal

Hide a growing service layer behind a **Facade**, and add a cross-cutting concern (execution-time logging) with
**aspect-oriented programming** instead of copy-pasted code.

## What was done

- **`ConstellationService`** (`service/`) now holds the constellation CRUD logic that used to live in
  `SpaceOperationCenterService`.
- **Facade** — `SpaceOperationCenterService` delegates to `ConstellationService` and `SatelliteService` and offers
  simple operations to the UI: `addSatellite(AddSatelliteRequest)` (creates the constellation when missing),
  `executeCommunicationMissions`, `executeImagingMissions`. `Main` talks only to the facade.
- Request objects: `service/AddSatelliteRequest`, `service/MissionRequest` with a `MissionType` enum.
- **AOP** — annotation `AOP/LogExecutionTime` and an `@Around` aspect `AOP/ExecutionTimeAspect` that logs how long
  an annotated method takes; applied to the report generation in `Main`.
- The service test was renamed to `ConstellationServiceTest`.

## How to run

```bash
./gradlew test
./gradlew bootRun
```

## Known issues

- `MissionRequest` is not used yet.
- `@MockBean` is deprecated in Spring Boot 3.4 (still works).
- `@EqualsAndHashCode` on mutable satellites can change equality while an object sits in a collection.
- "Emergency charging" in the special operations menu only prints that the battery level was raised to 50%; it does
  not change the battery level (there is no charging API in `EnergySystem` yet).
- The `test.mode` property only skips the "press Enter" prompt after initialization; it does not stop the menu.

## Fixed after the seminar

- The aspect never fired: its pointcut referred to `com.satellite.app.aspect.LogExecutionTime` while the annotation
  is in `com.satellite.app.AOP`, and the annotated method was a `private` method of `Main`, which a Spring proxy
  cannot intercept. The pointcut now names `com.satellite.app.AOP.LogExecutionTime`, the annotation is on the public
  facade methods `executeMissions`, `executeCommunicationMissions` and `executeImagingMissions`,
  `spring-boot-starter-aop` is declared explicitly, and `ExecutionTimeAspectTest` checks that the aspect runs.
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
  Spring context. It now uses `@SpringBootTest` with the `@Autowired` repository and `ConstellationService`.
  `Main` got the property `satellite.console.enabled` (default `true`); the test sets it to `false` so the console
  menu does not run.
- Comments in `application.properties` had broken encoding (`????`); rewritten in English.
- All text (docs, comments, messages, test names) translated to English.
