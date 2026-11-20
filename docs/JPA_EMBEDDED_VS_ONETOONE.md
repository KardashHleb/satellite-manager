# When to use @Embedded and when a separate table with @OneToOne?

A short guide to the same questions as in the assignment.

## Can the object exist on its own?

- **@Embedded** (or `@Embeddable`): the part **always** belongs to the parent entity; it has no separate "life" in the domain model. In the database its fields usually live in the parent's table (as columns of the parent table, possibly with a prefix).
- **Separate table + @OneToOne**: the child entity **can** be modeled as an independent object with its own primary key and its own row in its own table, even if in the business logic it does not exist without the parent.

## Is separate access to the object needed?

- If you need **queries on the sub-structure only** (reports, a "battery only" API, bulk updates without loading the whole satellite), a **separate table** and repository are more convenient.
- If the sub-structure is **never** queried separately and is always read together with the parent, **@Embedded** is simpler and gives fewer tables.

## Does the size of the main table matter?

- **@Embedded** makes **one** parent table bigger (many columns, wide rows). This can hurt caching, indexes and I/O when you select "the whole satellite", if there are many embedded fields.
- **@OneToOne** in a separate table: the main table stays compact; heavy or rarely needed data can be loaded **lazily** from the second table.

## Normalization requirements

- **3NF / separate entities in the domain** (for example the "energy system" as a tracked object with a change history) more often lead to a **separate table**.
- If it is a **pure value structure** without its own identity (coordinates, an amount + currency), it is a typical candidate for **@Embeddable**.

## What this project does

`EnergySystem` and `SatelliteState` are moved into **separate tables** with `@OneToOne` and their own repositories: separate CRUD operations and indexes on `satellite_id` make sense for them, and this fits better with the normalization requirement and the explicit Flyway schema.
