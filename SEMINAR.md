# Seminar 8 — Spring MVC and REST API

| | |
|---|---|
| Branch | `seminar_8` |
| Based on | [`seminar_7`](https://github.com/KardashHleb/satellite-manager/tree/seminar_7) |
| Last commit | 2026-04-04 |
| Next | [`seminar_9`](https://github.com/KardashHleb/satellite-manager/tree/seminar_9) (same MVC work, continued as microservices) |
| All seminars | [SEMINARS.md on main](https://github.com/KardashHleb/satellite-manager/blob/main/SEMINARS.md) |

## Learning goal

Turn the console application into a **web application**: expose the domain through a **REST API** built with
Spring MVC, document it with **OpenAPI / Swagger**, and add a simple browser page to call it.

## What was done

- **`SpaceOperationController`** (`controller/`), mapped under `/api`:

  | Method | Path | Purpose |
  |---|---|---|
  | GET | `/constellations` | all constellations |
  | GET | `/constellations/{name}` | one constellation |
  | POST | `/add-satellites` | add a satellite (creates the constellation when missing) |
  | POST | `/missions` | run a mission for a constellation or one satellite |
  | POST | `/constellations/{c}/satellites/{s}/activate` / `deactivate` | change satellite state |
  | POST | `/constellations/{c}/satellites/{s}/photo` | imaging satellite takes a photo |
  | POST | `/constellations/{c}/satellites/{s}/send-data?dataSize=` | communication satellite sends data |
  | GET | `/overview` | text status report |

- DTO `dto/MissionRequest`; domain classes moved into the `model/` package with the `SatelliteParam` hierarchy.
- springdoc-openapi 2.8.5 for Swagger UI; static test page `src/main/resources/static/test.html`.
- The console menu still exists as a `CommandLineRunner`.

## How to run

```bash
./gradlew bootRun
```

- Test page: http://localhost:8082/test.html
- Swagger UI: http://localhost:8082/swagger-ui/index.html
- Quick check: `curl http://localhost:8082/api/overview`

Port 8082 is used because 8080/8081 were busy on the development machine.

## Known issues

- The JPA starter is present but the datasource auto-configuration is excluded.
- `@MockBean` is deprecated in Spring Boot 3.4 (still works).
- `@EqualsAndHashCode` on mutable satellites can change equality while an object sits in a collection.
- "Emergency charging" in the special operations menu only prints that the battery level was raised to 50%; it does
  not change the battery level (there is no charging API in `EnergySystem` yet).
- The `test.mode` property only skips the "press Enter" prompt after initialization; it does not stop the menu.

## Fixed after the seminar

- `POST /api/add-satellites` always failed: `@JsonTypeInfo` consumes the JSON `type` property, so the no-arg
  constructors left `SatelliteParam.type` null and no factory matched; the constructors now set the type.
- `SpaceOperationController` answered 400 for every exception; it now returns 404 for a missing constellation or
  satellite (new `NotFoundException`), 400 for invalid input and 500 for unexpected errors. Emojis were removed
  from the responses. `SpaceOperationControllerTest` was added.
- The execution-time aspect never fired: its pointcut referred to `com.satellite.app.aspect.LogExecutionTime` while
  the annotation is in `com.satellite.app.AOP`, and the annotated method was a `private` method of `Main`. The
  pointcut was fixed, the annotation moved to the public facade mission methods, `spring-boot-starter-aop` is
  declared explicitly, and `ExecutionTimeAspectTest` checks that the aspect runs.
- `application.properties` contained "mission scheduler" settings with cron expressions that no code read; removed.
- `src/Satellite_constellation.iml` (an IntelliJ module file) was still tracked; it was removed from Git.
- The menu asked to "press Enter" before initialization as well as after it; only the prompt after initialization
  is left. On exit the repository contents were printed twice; they are now printed once.
- `gradle-wrapper.jar` was missing from Git; it is now tracked (done in the commit that added this file).
- The tracked `gradle-wrapper.jar` came from an older Gradle and had no `Main-Class`, while `gradlew` (8.14) runs it
  with `-jar`, so `./gradlew` failed with "no main manifest attribute"; the jar was replaced with the Gradle 8.14 one.
- `bootRun` did not forward the terminal input, so the console menu failed with "No line found"; `bootRun` and `run`
  now set `standardInput = System.in`.
- `./gradlew run` failed with "No main class specified"; `application { mainClass = 'com.satellite.app.Main' }` was added.
- `SatelliteParam.toString()` printed the 0..1 battery level as a percentage (`0.9%`); it is now multiplied by 100.
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
- All text (docs, comments, messages, test names) translated to English.
