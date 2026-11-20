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

- The local Gradle cache (`.gradle/8.14/...`) was committed by accident; it is removed in seminar 5.
- Comments in `application.properties` have broken encoding.
- The JPA starter is on the classpath although no database is used yet.
- `gradle/wrapper/gradle-wrapper.jar` was ignored by the `*.jar` rule in `.gitignore`, so `./gradlew` did not work
  on a fresh clone. **Fixed in the commit that added this file**: the jar is now tracked and excluded from the rule.
