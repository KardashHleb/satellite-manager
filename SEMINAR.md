# Seminar 11 — DevSecOps security audit

| | |
|---|---|
| Branch | `seminar_11_devSecAudit` |
| Based on | [`seminar_7`](https://github.com/KardashHleb/satellite-manager/tree/seminar_7); carries the work of seminars 8–11 (MVC, Docker, JPA, gRPC, Kafka) in one line |
| Last commit | 2026-05-30 |
| Next | [`seminar_11_fix`](https://github.com/KardashHleb/satellite-manager/tree/seminar_11_fix) |
| All seminars | [SEMINARS.md on main](https://github.com/KardashHleb/satellite-manager/blob/main/SEMINARS.md) |

## Learning goal

Run a **security audit** of the whole project with open-source DevSecOps tools: secrets in the code and history,
static analysis, software bill of materials (SBOM), vulnerable dependencies and a dynamic scan of a running service.

## What the branch contains

This branch is the "main line" of the project: it brings together the results of seminars 8–11 on top of
seminar 7 — five Gradle modules (`server`, `mission-service`, `telemetry-service`, `telemetry-proto`,
`satellite-events`), REST + Swagger, JPA + Flyway + PostgreSQL, gRPC telemetry, Kafka events, Docker Compose.

## What was done for the audit

| Tool | What it checks | Report | Result |
|---|---|---|---|
| Gitleaks | secrets in files and in Git history | `gitleaks-report.json`, `gitleaks-git-report.json` | nothing found |
| Semgrep | static code analysis | `semgrep-report.json` | 0 findings |
| Syft | SBOM | `sbom.json` | generated |
| Grype | known CVEs in dependencies / images | `grype-report.json`, `.grype.yaml` | 0 matches |
| OWASP ZAP | dynamic scan of a running service (baseline and full) | `zap-baseline-report.html`, `zap-full-report.html/json` | see below |

- **ZAP full scan** of `telemetry-service` (port 8084): Medium "HTTP only site", Medium "Spring Actuator
  information leak", Low missing `Cross-Origin-Resource-Policy` and `X-Content-Type-Options` headers.
- `.grype.yaml` fails the scan only on critical vulnerabilities and documents one accepted CVE.
- **`README-SEC.md`** explains how to install and run every tool, including scanning Docker images
  (`docker save` + `grype docker-archive:`).
- Also: the Gradle wrapper was changed to 8.5, `.tar` image archives were removed from Git, Docker and
  `.gitignore` were tidied up.

## How to run

```bash
docker compose up --build
```

Then follow `README-SEC.md` to repeat each scan. Services: 8082 (server, Swagger), 8083 (mission-service),
8084 / 9091 (telemetry HTTP / gRPC), 9092 (Kafka).

## Known issues

- The SBOM was generated from the source folder and lists no Gradle dependencies, so the clean Grype result is
  not meaningful; scanning the built images or jars gives a real picture.
- The reports are committed in the repository root (moved to `docs/securityReports/` in seminar 11 performance testing).
- `gradle-wrapper.jar` was missing from Git, which broke `./gradlew` and the Docker build on a fresh clone;
  **fixed in the commit that added this file**.
