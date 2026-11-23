# @Embedded vs @OneToOne: choosing a mapping for the satellite energy system and state

In the **server** project the `Satellite` entity has two related objects:

- `EnergySystem` — battery level (`battery_level`);
- `SatelliteState` — active flag (`is_active`).

They are modeled as **separate entities** with a **`@OneToOne`** relationship, not as **`@Embedded`** embeddable types.

---

## Current design (@OneToOne)

```java
// Satellite.java (excerpt)
@OneToOne(mappedBy = "satellite", cascade = CascadeType.ALL, orphanRemoval = true, optional = false)
private EnergySystem energy;

@OneToOne(mappedBy = "satellite", cascade = CascadeType.ALL, orphanRemoval = true, optional = false)
private SatelliteState state;
```

The owning side of the relationship is `EnergySystem` and `SatelliteState` (the `satellite` field + `@JoinColumn(name = "satellite_id")`).

In the database (Flyway `V1__init_schema.sql`):

| Table | Purpose |
|-------|---------|
| `satellite` | base satellite record |
| `energy_system` | `satellite_id` UNIQUE → FK to `satellite` |
| `satellite_state` | `satellite_id` UNIQUE → FK to `satellite` |

Advantages of this approach in this project:

1. **Normalization (3NF)** — energy and state are standalone tables with their own `id`; they can be updated and queried separately (see the CRUD controllers `EnergySystemCrudController`, `SatelliteStateCrudController`).
2. **Spring Data repositories** — `EnergySystemRepository`, `SatelliteStateRepository` work without workarounds through the parent entity.
3. **Extensibility** — new fields (for example, the temperatures added to `satellite` in `V2__add_satellite_temperatures.sql`) do not get mixed with the battery/activity logic.
4. **Cascades** — `cascade = ALL` and `orphanRemoval = true` on the `Satellite` side preserve the "one satellite — one energy system / one state" lifecycle.

Disadvantages: more tables and JOINs when loading a whole satellite (with `FetchType.LAZY` on `constellation` the load stays manageable).

---

## Alternative: @Embedded / @Embeddable

If `EnergySystem` and `SatelliteState` were **value types without their own identity**, they could be described like this:

```java
@Embeddable
public class BatteryInfo {
    private double batteryLevel;
}

@Embeddable
public class OperationalFlags {
    private boolean active;
}

@Entity
public class Satellite {
    @Embedded
    private BatteryInfo energy;

    @Embedded
    private OperationalFlags state;
}
```

The `battery_level` and `is_active` columns would live **in the `satellite` table** (or be mapped via `@AttributeOverrides` if the columns are renamed).

Advantages of `@Embedded`:

- a single table — a satellite can be read with one SELECT;
- fewer entities in the persistence model.

Disadvantages for **this** project:

- the energy system/state has no separate `id` → CRUD and battery-only reports become harder;
- responsibilities get mixed in one table as the number of attributes grows;
- two `@Embedded` types with identical field names would require `@AttributeOverrides`.

---

## Comparison

| Criterion | @OneToOne (current project) | @Embedded |
|-----------|-----------------------------|-----------|
| Tables | `satellite` + `energy_system` + `satellite_state` | usually just `satellite` |
| Separate CRUD | yes | no (only through `Satellite`) |
| FK / integrity | explicit `satellite_id` UNIQUE | columns in the satellite row |
| Typical use case | sub-entity with its own lifecycle | composition, "part of the aggregate" |

---

## Why satellite-manager uses @OneToOne

1. The Flyway schema was designed from the start with **three tables** linked by FKs — this matches the JPA entities, not embeddable fields in `satellite`.
2. The course assignment includes **REST CRUD** for the energy system and state — separate `@Entity` classes are more convenient for that.
3. Telemetry updates fields on `Satellite` (`temperature_*`), while the battery and activity stay in their own tables — the domain boundaries remain clear.

`@Embedded` would be appropriate if the battery and the "enabled" flag were treated as an immutable part of the satellite row, with no separate APIs and no plans for separate repositories.

---

## Related files

- `server/src/main/java/com/satellite/app/model/Satellite.java`
- `server/src/main/java/com/satellite/app/EnergySystem.java`
- `server/src/main/java/com/satellite/app/SatelliteState.java`
- `server/src/main/resources/db/migration/V1__init_schema.sql`
