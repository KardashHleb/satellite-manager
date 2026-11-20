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

- `application.properties` contains "mission scheduler" settings with cron expressions that no code reads yet.
- The JPA starter is present but the datasource auto-configuration is excluded.
- `src/Satellite_constellation.iml` is tracked.
- `gradle-wrapper.jar` was missing from Git; **fixed in the commit that added this file**.
