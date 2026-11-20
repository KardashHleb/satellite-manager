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

- The aspect's pointcut refers to `com.satellite.app.aspect.LogExecutionTime`, but the annotation is in
  `com.satellite.app.AOP`, and the annotated method is `private` (Spring proxies cannot intercept it). The aspect
  most likely never fires in this branch; later branches add `AspectProxyConfig`.
- `MissionRequest` is not used yet.
- `gradle-wrapper.jar` was missing from Git; **fixed in the commit that added this file**.
