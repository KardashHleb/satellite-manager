# Seminar 11 — Load testing with k6 and Allure

| | |
|---|---|
| Branch | `seminar_11_performance_testing` |
| Based on | [`seminar_11_fix`](https://github.com/KardashHleb/satellite-manager/tree/seminar_11_fix) |
| Last commit | 2026-06-06 |
| Next | [`seminar_12`](https://github.com/KardashHleb/satellite-manager/tree/seminar_12) |
| All seminars | [SEMINARS.md on main](https://github.com/KardashHleb/satellite-manager/blob/main/SEMINARS.md) |

## Learning goal

Measure how the API behaves **under load**: script a realistic user flow in **k6**, define pass/fail
**thresholds**, and publish the results as an **Allure** report.

## What was done

- **`load-tests/scenario.js`** — an operator flow against `BASE_URL` (default `http://localhost:8082`):
  overview → read the demo constellation → create a constellation → add a satellite → rename the constellation →
  delete the satellite → delete the constellation. Every virtual user works with its own constellation
  (`k6-c-{VU}-{ITER}`), so users do not interfere with each other.
- **Load profiles and thresholds**:

  | Profile | Load | Thresholds |
  |---|---|---|
  | `smoke1` | 1 virtual user, 30 s | failed requests < 1% |
  | `smoke2` | 2 virtual users, 30 s | failed requests < 1% |
  | `load10` | ramp up to 10 users, hold 60 s | failed requests < 5%, p95 latency < 3000 ms |

- **Reporting**: `load-tests/lib/allure-summary.js` turns the k6 summary into Allure results (one step per scenario
  group plus a metrics attachment); `package.json` provides the scripts and `allure-commandline`.
- **Docs**: security reports moved to `docs/securityReports/`, extra guides to `docs/AdditionalREADME/`
  (Docker, security, draft); the README documents the scenario.

## How to run

Start the system, then run k6 (requires k6 and Node.js):

```bash
docker compose up --build
cd load-tests
npm install
npm run test:1      # smoke, 1 user
npm run test:10     # load, 10 users
npm run report      # build the Allure report
npm run serve       # open it
```

Against another host: `k6 run -e BASE_URL=http://host:8082 -e PROFILE=load10 scenario.js`.

## Known issues

- The Allure status is "failed" as soon as any request fails, even if the k6 thresholds pass.
- `gradle-wrapper.jar` was missing from Git; **fixed in the commit that added this file**.
