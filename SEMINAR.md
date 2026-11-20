# Seminar 10 — gRPC streaming telemetry

| | |
|---|---|
| Branch | `seminar_10` |
| Based on | [`seminar_9.2`](https://github.com/KardashHleb/satellite-manager/tree/seminar_9.2) |
| Last commit | 2026-05-10 |
| Next | [`seminar_11`](https://github.com/KardashHleb/satellite-manager/tree/seminar_11) contains the same gRPC commit and continues with Kafka |
| All seminars | [SEMINARS.md on main](https://github.com/KardashHleb/satellite-manager/blob/main/SEMINARS.md) |

## Learning goal

Connect services with **gRPC** instead of REST: describe the contract in **Protocol Buffers** and use a
**server-streaming** RPC to push telemetry continuously.

## What was done

- **New module `telemetry-service`** (HTTP 8084, gRPC 9091):
  - contract `src/main/proto/telemetry.proto` with the service `StreamTelemetry`;
  - `TelemetryGrpcService` (`@GrpcService`) streams random inside and outside temperatures every 2 seconds.
- **`server`** as a gRPC client:
  - protobuf Gradle plugin and gRPC client starter;
  - `TelemetryStreamClient` subscribes when the application is ready, reconnects on errors, and stores the
    temperatures on the satellites;
  - migration `V2__add_satellite_temperatures.sql` adds the two temperature columns.
- **Missions**: `MissionTargetType` enum (whole constellation or one satellite); the mission logic moved from the
  controller into `SpaceOperationCenterService.executeMission`.
- **Compose**: network renamed to `space-net`, `telemetry-service` added, server gets `TELEMETRY_GRPC_ADDRESS`.

## How to run

```bash
docker compose up --build
```

- Telemetry service health: http://localhost:8084/actuator/health
- Watch the server log: temperatures arrive every 2 seconds.

## Known issues

- The commit is titled "suffering from gRPC": this was the hardest step, and the code is a first working version.
- The satellite names in the stream are hard-coded (fixed in seminar 11 with a Kafka-fed registry).
- The `.proto` file is duplicated in both modules (seminar 11 introduces a shared `telemetry-proto` module), and the
  gRPC starter versions differ between client (3.0.0) and server (2.15.0).
- `README.md` was emptied and `docs/JPA_EMBEDDED_VS_ONETOONE.md` deleted in this branch; both are restored in seminar 11.
- `gradle-wrapper.jar` was missing from Git; **fixed in the commit that added this file**.
