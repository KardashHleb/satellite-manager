# Seminar 2 — SOLID refactoring

| | |
|---|---|
| Branch | `seminar_2` |
| Based on | `main` (initial console app) |
| Last commit | 2026-01-22 |
| Next | [`seminar_3`](https://github.com/KardashHleb/satellite-manager/tree/seminar_3) |
| All seminars | [SEMINARS.md on main](https://github.com/KardashHleb/satellite-manager/blob/main/SEMINARS.md) |

## Learning goal

Apply the SOLID principles, first of all the **Single Responsibility Principle**, to a small object-oriented
console application, and clean up the repository.

## What was done

- **`EnergySystem`** (`src/EnergySystem.java`) was extracted from `Satellite`. It owns the battery level,
  its percentage and `consumeBattery()`, and keeps the value within 0..1.
- **`SatelliteState`** (`src/SatelliteState.java`) was extracted as well. It owns the active flag and the
  `activate()` / `deactivate()` transitions.
- **`Satellite`** now delegates to these two objects instead of keeping raw `isActive` / `batteryLevel` fields.
  `CommunicationSatellite` and `ImagingSatellite` use the getters and switch the satellite off when the battery runs out.
- IDE files (`.idea/`) and compiled classes (`out/`) were removed from Git; a `.gitignore` was added.

## How to run

No build tool yet, plain `javac`:

```bash
javac -d out src/*.java
java -cp out Main
```

## Known issues

- `src/Satellite_constellation.iml` (an IntelliJ module file) is still tracked.
