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
- `gradle-wrapper.jar` was missing from Git; **fixed in the commit that added this file**.
