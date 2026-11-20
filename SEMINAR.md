# Seminar 5 — GoF patterns, part 1: Builder and Factory Method

| | |
|---|---|
| Branch | `seminar_5` |
| Based on | [`seminar_3`](https://github.com/KardashHleb/satellite-manager/tree/seminar_3), includes the testing commit of [`seminar_4`](https://github.com/KardashHleb/satellite-manager/tree/seminar_4) |
| Last commit | 2026-02-17 |
| Next | [`seminar_6`](https://github.com/KardashHleb/satellite-manager/tree/seminar_6) |
| All seminars | [SEMINARS.md on main](https://github.com/KardashHleb/satellite-manager/blob/main/SEMINARS.md) |

## Learning goal

Apply the creational **Gang of Four** patterns to the domain model: **Builder** for a validated value object and
**Factory Method** for creating satellites of different types.

## What was done

- Everything from seminar 4: tests, JaCoCo and Lombok.
- **Builder** — `EnergySystem` has a private constructor, a static `builder()` and an inner `EnergySystemBuilder`.
  The builder validates the battery level (0..1) and throws instead of silently clamping. `Satellite` creates its
  energy system through the builder.
- **Factory Method** — `factory/SatelliteFactory` (interface) with two Spring `@Component` implementations,
  `CommunicationSatelliteFactory` and `ImagingSatelliteFactory`. `Main` creates satellites only through factories.
- Repository hygiene: the accidentally committed `.gradle/` cache was removed and added to `.gitignore`.

## How to run

```bash
./gradlew test
./gradlew bootRun
```

## Known issues

- Some factory shortcuts (`createWithBandwidth`, `createWithResolution`, `createSatellite(name)`) pass a battery
  level of `100.0`; the new builder rejects anything above `1.0`, so these calls throw `IllegalArgumentException`.
- In the menu, a battery level above 1 now raises an error instead of being clamped.
- `gradle-wrapper.jar` was missing from Git; **fixed in the commit that added this file**.
