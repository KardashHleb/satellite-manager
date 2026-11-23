# Seminar 9 (extra) — API checks with Postman test scripts

| | |
|---|---|
| Branch | `seminar_9_postmanExample` (side branch) |
| Based on | [`seminar_9`](https://github.com/KardashHleb/satellite-manager/tree/seminar_9) |
| Last commit | 2026-05-09 |
| All seminars | [SEMINARS.md on main](https://github.com/KardashHleb/satellite-manager/blob/main/SEMINARS.md) |

## Learning goal

Write **automated API checks in Postman**: assert status codes and bodies, pass data between requests with
collection variables, and verify that the gateway service returns exactly what the server returns.

## What was done

One file was added: [`postman-scripts.js`](postman-scripts.js). It holds two Postman "Tests" scripts:

1. For `GET {{serverBaseUrl}}/api/overview`: the status is 200, the body is not empty, and the body is saved
   to the collection variable `overviewFromServer`.
2. For `GET {{missionBaseUrl}}/api/remote/overview`: the status is 200 and the body **equals** the saved server response,
   which proves that `mission-service` proxies the server correctly.

The file also explains the variables: `serverBaseUrl = http://localhost:8082`, `missionBaseUrl = http://localhost:8083`.

## How to run

1. Start the services as in seminar 9: `docker compose up --build`.
2. In Postman, create a collection with the two requests, set the two variables, and paste each script into the
   **Tests** tab of its request.
3. Run the collection (Collection Runner) in order.

## Known issues

- There is no exported Postman collection or environment file; the collection has to be built by hand.
- The branch is based on seminar 9, so it has no database yet.
- The console menu item "Emergency satellite charging" only prints which satellites would be charged; it does not change the battery level.

## Fixed after the seminar

The server fixes are the same as in [`seminar_9`](https://github.com/KardashHleb/satellite-manager/tree/seminar_9):

- `gradle-wrapper.jar` was missing from Git (this broke `./gradlew` and the Docker build), and the jar added later had no `Main-Class`; it is now the Gradle 8.14 wrapper jar.
- `./gradlew :server:bootRun` failed with "No line found" in the console menu; `bootRun` now forwards `System.in`.
- The console menu asked to press Enter before and after the start-up banner and printed the repository contents twice on exit; it now asks once and prints them once.
- `POST /api/add-satellites` always failed: `@JsonTypeInfo` consumes the JSON `type` property, so the no-arg constructors of the parameter classes left `type` null and no factory matched; they now set the type.
- `SpaceOperationController` answered every error with 400 and emoji strings; it now returns 404 for an unknown constellation or satellite (new `NotFoundException`), 400 for bad input, 500 otherwise, without emojis (test: `SpaceOperationControllerTest`).
- `Satellite.activate()` on an already active satellite printed "battery level is low", and the console "activate by type" counted satellites that failed to activate; it now reports "already active" and counts only satellites that were turned on (test: `SatelliteActivationTest`).
- The console "add a new satellite" menu accepted a battery level outside 0.0-1.0, which crashed with an exception; it now asks again.
- `SatelliteParam.toString()` printed the 0..1 battery level as a percentage (for example "0.8%"); it now prints "80%".
- All text (docs, comments, messages, test names) translated to English.
