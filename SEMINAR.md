# Seminar 3 — Gradle, Spring Boot and layered architecture

| | |
|---|---|
| Branch | `seminar_3` |
| Based on | [`seminar_2`](https://github.com/KardashHleb/satellite-manager/tree/seminar_2) |
| Last commit | 2026-01-25 |
| Next | [`seminar_4`](https://github.com/KardashHleb/satellite-manager/tree/seminar_4) (testing), [`seminar_5`](https://github.com/KardashHleb/satellite-manager/tree/seminar_5) (patterns) |
| All seminars | [SEMINARS.md on main](https://github.com/KardashHleb/satellite-manager/blob/main/SEMINARS.md) |

## Learning goal

Move from hand-compiled sources to a **Gradle** build, introduce **Spring Boot** with dependency injection,
and split the code into **repository** and **service** layers.

## What was done

- Sources moved to the standard layout `src/main/java/com/satellite/app/`.
- `build.gradle`: Java 21, Spring Boot 3.4.2, dependency management, `application` plugin; `settings.gradle`
  names the project `satellite-manager`. The Gradle wrapper (`gradlew`, Gradle 8.14) was added.
- **`ConstellationRepository`** (`repository/`): an in-memory `HashMap` store annotated with `@Repository`.
- **`SpaceOperationCenterService`** (`service/`): a `@Service` with constellation CRUD and satellite operations,
  injected into the application.
- **`Main`** became a `@SpringBootApplication` with a `CommandLineRunner` interactive console menu.
- `application.properties` disables the datasource auto-configuration (no database yet) and the banner.

## How to run

```bash
./gradlew bootRun
```

Main class: `com.satellite.app.Main`. There are no tests yet.

## Known issues

- The JPA starter is on the classpath although no database is used yet.
- "Emergency charging" in the special operations menu only prints that the battery level was raised to 50%; it does
  not change the battery level (there is no charging API in `EnergySystem` yet).

## Fixed after the seminar

- `gradle/wrapper/gradle-wrapper.jar` was ignored by the `*.jar` rule in `.gitignore`, so `./gradlew` did not work
  on a fresh clone; the jar is now tracked and excluded from the rule (done in the commit that added this file).
- The tracked `gradle-wrapper.jar` came from an older Gradle and had no `Main-Class`, while `gradlew` (8.14) runs it
  with `-jar`, so `./gradlew` failed with "no main manifest attribute"; the jar was regenerated for Gradle 8.14.
- `./gradlew run` failed with "No main class specified"; `application { mainClass = 'com.satellite.app.Main' }` was added.
- `bootRun` / `run` did not forward the terminal input, so the console menu crashed at once with
  `NoSuchElementException`; both tasks now set `standardInput = System.in`.
- The local Gradle cache (`.gradle/8.14/...`) was committed by accident; it was removed from Git and `.gradle/` was
  added to `.gitignore`.
- `src/Satellite_constellation.iml` (an IntelliJ module file) was still tracked; it was removed from Git.
- Comments in `application.properties` had broken encoding (`????`); rewritten in English.
- `Satellite.activate()` printed "low battery" for a satellite that was already active; it now says that the
  satellite is already active.
- "Activate by type" counted every satellite of the type, even those that failed to activate; it now counts only
  satellites that were actually activated.
- All text (docs, comments, messages, test names) translated to English.
