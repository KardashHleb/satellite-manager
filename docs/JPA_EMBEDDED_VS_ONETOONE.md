# @Embedded and @OneToOne: the choice for the satellite energy system and state

In the **server** project the `Satellite` entity has two related objects:

- `EnergySystem` — battery level (`battery_level`);
- `SatelliteState` — activity flag (`is_active`).

They are modeled as **separate entities** with a **`@OneToOne`** relationship, not as **`@Embedded`** embeddable types.

---

## How it works now (@OneToOne)

```java
// Satellite.java (fragment)
@OneToOne(mappedBy = "satellite", cascade = CascadeType.ALL, orphanRemoval = true, optional = false)
private EnergySystem energy;

@OneToOne(mappedBy = "satellite", cascade = CascadeType.ALL, orphanRemoval = true, optional = false)
private SatelliteState state;
```

The owners of the relationship are `EnergySystem` and `SatelliteState` (the `satellite` field + `@JoinColumn(name = "satellite_id")`).

In the database (Flyway `V1__init_schema.sql`):

| Table | Purpose |
|---------|------------|
| `satellite` | base satellite record |
| `energy_system` | `satellite_id` UNIQUE → FK to `satellite` |
| `satellite_state` | `satellite_id` UNIQUE → FK to `satellite` |

Advantages of this approach in this project:

1. **Normalization (3NF)** — energy and state are separate tables with their own `id`; they can be updated and queried separately (see the CRUD controllers `EnergySystemCrudController`, `SatelliteStateCrudController`).
2. **Spring Data repositories** — `EnergySystemRepository`, `SatelliteStateRepository` without workarounds through the parent entity.
3. **Extensibility** — new fields (for example the temperatures added to `satellite` in `V2__add_satellite_temperatures.sql`) are not mixed with the battery/activity logic.
4. **Cascades** — `cascade = ALL` and `orphanRemoval = true` on the `Satellite` side keep the "one satellite — one energy system / one state" lifecycle.

Disadvantages: more tables and JOINs when the whole satellite is loaded (with `FetchType.LAZY` on `constellation` the load stays under control).

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

The columns `battery_level`, `is_active` would be **in the `satellite` table** (or renamed with `@AttributeOverrides`).

Advantages of `@Embedded`:

- one table — a satellite is read with one SELECT;
- fewer entities in the persistence model.

Disadvantages for **this** project:

- the energy system/state have no `id` of their own → CRUD and battery-only reports are harder;
- mixed responsibilities in one table as the number of attributes grows;
- two `@Embedded` types with the same field names would need `@AttributeOverrides`.

---

## Comparison

| Criterion | @OneToOne (current project) | @Embedded |
|----------|----------------------------|-----------|
| Tables | `satellite` + `energy_system` + `satellite_state` | usually one `satellite` |
| Separate CRUD | yes | no (only through `Satellite`) |
| FK / integrity | explicit `satellite_id` UNIQUE | columns in the satellite row |
| Typical case | a sub-entity with its own lifecycle | composition, "part of an aggregate" |

---

## Why satellite-manager uses @OneToOne

1. The Flyway schema was designed from the start for **three tables** with FKs — this matches JPA entities, not embeddable fields in `satellite`.
2. The assignment includes **REST CRUD** for the energy system and the state — separate `@Entity` classes are more convenient.
3. Telemetry updates fields on `Satellite` (`temperature_*`), while the battery and activity stay in their own tables — the domain boundaries stay clear.

`@Embedded` would fit if the battery and the "on" flag were an unchangeable part of the satellite row, with no separate APIs and no plans for separate repositories.

---

## Related files

- `server/src/main/java/com/satellite/app/model/Satellite.java`
- `server/src/main/java/com/satellite/app/EnergySystem.java`
- `server/src/main/java/com/satellite/app/SatelliteState.java`
- `server/src/main/resources/db/migration/V1__init_schema.sql`
