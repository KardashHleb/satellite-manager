# Seminar 11 (extra) — Integration with an API test-automation project

| | |
|---|---|
| Branch | `seminar_11_api_automation` |
| Based on | [`seminar_11`](https://github.com/KardashHleb/satellite-manager/tree/seminar_11) |
| Last commit | 2026-05-24 |
| Next | [`seminar_11_devSecAudit`](https://github.com/KardashHleb/satellite-manager/tree/seminar_11_devSecAudit) continues the main line |
| All seminars | [SEMINARS.md on main](https://github.com/KardashHleb/satellite-manager/blob/main/SEMINARS.md) |

## Learning goal

Prepare the system to be tested by a **separate automated API test suite** (Gradle + Allure reports), and fix the
API behavior that those tests exposed.

## What was done

- **README**: how to run the stack and then the external `api-automation` project against it
  (`./gradlew clean test`, then `allureServe` or `allureReport`); base URLs 8082, 8083, 8084.
- **Fixes found by the automated tests**:
  - `ConstellationService.addSatellite` now uses `saveAndFlush` and reads the saved satellite back, so the Kafka
    event carries the real database id instead of `null`;
  - `CrudManagementService.addSatelliteFromRequest` returns the saved entity (and a 500 error if it cannot be found);
  - `ConstellationServiceTest` updated accordingly.

## How to run

```bash
docker compose up --build
```

Then, in the separate api-automation repository:

```bash
./gradlew clean test
./gradlew allureServe
```

## Known issues

- The automated tests live in another repository and are not included here.
- `gradle-wrapper.jar` was missing from Git; **fixed in the commit that added this file**.
