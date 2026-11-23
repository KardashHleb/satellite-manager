# Satellite Manager


## Running the application

```bash
docker compose up --build
```

API: http://localhost:8082/swagger-ui/index.html

## Load testing (k6 + Allure)

**Requirements:**

- Docker
- k6
- Node.js
- Java





On the first run, k6 downloads the official `textSummary` utility from the Grafana CDN:
`https://jslib.k6.io/k6-summary/0.0.2/index.js` (see `load-tests/lib/allure-summary.js`).
It formats the final test summary for the console; Allure files are written separately.
Internet access is required on the first run (after that k6 can use its cache).

Before running the commands below, go to `load-tests` (from the repository root):

```bash
cd load-tests
```
### Setup (once)

```bash
npm install
```
`npm install` downloads the **Allure CLI** into `node_modules` (the `allure-commandline` package). It is needed for `npm run serve` and `npm run report` — there is no need to install Allure globally. Allure, like the application itself, requires Java.

Then use one of the following commands to run the tests

```bash
npm run test:1      # 1 user, 30 s
npm run test:2      # 2 users, 30 s
npm run test:10     # ramp up to 10, hold for 60 s
npm run report      # static HTML in allure-report/
npm run serve       # allure serve — report in the browser (from allure-results/)
npm run test:all    # test:10 + serve (report in the browser)
```

A different server URL (also from `load-tests`):

```bash
cd load-tests
k6 run -e BASE_URL=http://127.0.0.1:8082 -e PROFILE=load10 scenario.js
```

Docker details — [README-DOCKER.md](docs/AdditionalREADME/README-DOCKER.md).


## User scenario (load test)

A mission control center operator goes through the chain of actions in `load-tests/scenario.js`. Each step is a separate k6 group and a REST API call:

| Step | Operator action | API |
|-----|-------------------|-----|
| 1 | System overview | `GET /api/overview` |
| 2 | View the RU Basic constellation | `GET /api/constellations/RU Basic` |
| 3 | Create a constellation | `POST /api/crud/constellations` |
| 4 | Add a satellite to the constellation | `POST /api/crud/satellites` |
| 5 | Rename the constellation | `PUT /api/crud/constellations/{name}` |
| 6 | Delete the satellite | `DELETE /api/crud/satellites/{id}` |
| 7 | Delete the constellation | `DELETE /api/crud/constellations/{name}` |

In steps 3–7 each virtual user works with its own constellation (`k6-c-{VU}-{ITER}`); the rename is done before deletion to verify updating by the new name.

Requests with a body use the `Content-Type: application/json` header.

### 1. System overview

`GET /api/overview`

No request body is needed.

**Response:** `200 OK`, a text report on all constellations.

```text
=== Space Operation Center Overview ===

=== Constellation status 'RU Basic' ===
Total satellites: 5
...
```

### 2. View the RU Basic constellation

`GET /api/constellations/RU%20Basic`

**Response:** `200 OK`, constellation JSON (the `satellites` field may be empty in serialization due to lazy associations):

```json
{
  "id": 1,
  "constellationName": "RU Basic",
  "satellites": []
}
```

If the constellation does not exist — `404 Not Found`.

### 3. Create a constellation

`POST /api/crud/constellations`

```json
{
  "constellationName": "k6-c-1-0"
}
```

**Response:** `201 Created`

```json
{
  "id": 2,
  "constellationName": "k6-c-1-0",
  "satellites": []
}
```

### 4. Add a satellite

`POST /api/crud/satellites`

```json
{
  "constellationName": "k6-c-1-0",
  "satelliteParam": {
    "type": "COMMUNICATION",
    "name": "k6-s-1-0",
    "batteryLevel": 0.8,
    "bandwidth": 500
  }
}
```

**Response:** `201 Created` — the body contains the created satellite; the `id` field is used in step 6:

```json
{
  "id": 42,
  "name": "k6-s-1-0",
  "temperatureInside": null,
  "temperatureOutside": null,
  "bandwidth": 500.0,
  "dataSent": 0.0
}
```

### 5. Rename the constellation

`PUT /api/crud/constellations/k6-c-1-0`

```json
{
  "newName": "k6-c-1-0-renamed"
}
```

**Response:** `200 OK`

```json
{
  "id": 2,
  "constellationName": "k6-c-1-0-renamed",
  "satellites": []
}
```

### 6. Delete the satellite

`DELETE /api/crud/satellites/42`

No request body is needed (`42` is the `id` from step 4).

**Response:** `204 No Content`

### 7. Delete the constellation

`DELETE /api/crud/constellations/k6-c-1-0-renamed`

No request body is needed (the name after the rename in step 5).

**Response:** `204 No Content`
