# Seminar 11 — Fixes after the security scan

| | |
|---|---|
| Branch | `seminar_11_fix` |
| Based on | [`seminar_11_devSecAudit`](https://github.com/KardashHleb/satellite-manager/tree/seminar_11_devSecAudit) |
| Last commit | 2026-05-31 |
| Next | [`seminar_11_performance_testing`](https://github.com/KardashHleb/satellite-manager/tree/seminar_11_performance_testing) |
| All seminars | [SEMINARS.md on main](https://github.com/KardashHleb/satellite-manager/blob/main/SEMINARS.md) |

## Learning goal

Close the loop of a security audit: **fix** what the scanners found, then **scan again** to confirm.

## What was done

- **Dependency update**: Spring Boot `3.4.2` → `3.4.13` in `build.gradle`, the usual remedy for known CVEs in
  framework and transitive dependencies.
- **Line endings**: `.gitattributes` forces LF for `gradlew` and `gradle/wrapper/**`, and all three Dockerfiles run
  `sed -i 's/\r$//' gradlew` before building. Without this, a Windows checkout produced a `gradlew` script that
  could not run inside Linux containers.
- **Re-scan**: `gitleaks-git-report2.json` (no secrets) and `semgrep-report2.json`.

## How to run

```bash
docker compose up --build
```

## Known issues

- The 15 warnings in `semgrep-report2.json` are all `plaintext-http-link` inside the committed ZAP HTML reports, not
  in the application code.
- The ZAP findings (actuator information, missing security headers) are not addressed yet.
- `gradle-wrapper.jar` was missing from Git; **fixed in the commit that added this file**.
