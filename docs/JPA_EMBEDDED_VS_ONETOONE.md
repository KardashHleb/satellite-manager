# @Embedded и @OneToOne: выбор для энергосистемы и состояния спутника

В проекте **server** у сущности `Satellite` два связанных объекта:

- `EnergySystem` — заряд батареи (`battery_level`);
- `SatelliteState` — флаг активности (`is_active`).

Они смоделированы как **отдельные сущности** со связью **`@OneToOne`**, а не как встраиваемые типы **`@Embedded`**.

---

## Как устроено сейчас (@OneToOne)

```java
// Satellite.java (фрагмент)
@OneToOne(mappedBy = "satellite", cascade = CascadeType.ALL, orphanRemoval = true, optional = false)
private EnergySystem energy;

@OneToOne(mappedBy = "satellite", cascade = CascadeType.ALL, orphanRemoval = true, optional = false)
private SatelliteState state;
```

Владельцем связи выступают `EnergySystem` и `SatelliteState` (поле `satellite` + `@JoinColumn(name = "satellite_id")`).

В БД (Flyway `V1__init_schema.sql`):

| Таблица | Назначение |
|---------|------------|
| `satellite` | базовая запись спутника |
| `energy_system` | `satellite_id` UNIQUE → FK на `satellite` |
| `satellite_state` | `satellite_id` UNIQUE → FK на `satellite` |

Плюсы такого подхода в этом проекте:

1. **Нормализация (3НФ)** — энергия и состояние — самостоятельные таблицы с собственным `id`, их можно обновлять и запрашивать отдельно (см. CRUD-контроллеры `EnergySystemCrudController`, `SatelliteStateCrudController`).
2. **Репозитории Spring Data** — `EnergySystemRepository`, `SatelliteStateRepository` без обходных путей через родительскую сущность.
3. **Расширяемость** — новые поля (например, температуры в `V2__add_satellite_temperatures.sql` на `satellite`) не смешиваются с логикой батареи/активности.
4. **Каскады** — `cascade = ALL` и `orphanRemoval = true` на стороне `Satellite` сохраняют жизненный цикл «один спутник — одна энергосистема / одно состояние».

Минусы: больше таблиц и JOIN при загрузке спутника целиком (при `FetchType.LAZY` на `constellation` нагрузка контролируема).

---

## Альтернатива: @Embedded / @Embeddable

Если бы `EnergySystem` и `SatelliteState` были **значимыми типами без собственной идентичности**, их можно было бы описать так:

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

Колонки `battery_level`, `is_active` лежали бы **в таблице `satellite`** (или в `@AttributeOverrides` при переименовании колонок).

Плюсы `@Embedded`:

- одна таблица — проще читать спутник одним SELECT;
- меньше сущностей в модели persistence.

Минусы для **данного** проекта:

- нет отдельного `id` у энергосистемы/состояния → сложнее CRUD и отчёты только по батарее;
- смешение ответственности в одной таблице при росте атрибутов;
- два `@Embedded` с одинаковыми именами полей потребуют `@AttributeOverrides`.

---

## Сравнение

| Критерий | @OneToOne (текущий проект) | @Embedded |
|----------|----------------------------|-----------|
| Таблицы | `satellite` + `energy_system` + `satellite_state` | обычно одна `satellite` |
| Отдельный CRUD | да | нет (только через `Satellite`) |
| FK / целостность | явный `satellite_id` UNIQUE | колонки в строке спутника |
| Типичный случай | подсущность с собственным жизненным циклом | композиция «часть агрегата» |

---

## Почему в satellite-manager выбран @OneToOne

1. Схема Flyway изначально спроектирована под **три таблицы** с FK — это согласовано с JPA, а не с embeddable-полями в `satellite`.
2. Учебная задача включает **REST CRUD** по энергосистеме и состоянию — отдельные `@Entity` удобнее.
3. Телеметрия обновляет поля на `Satellite` (`temperature_*`), а батарея и активность остаются в своих таблицах — границы домена остаются чёткими.

`@Embedded` был бы уместен, если бы батарея и флаг «включён» считались неизменяемой частью строки спутника без отдельных API и без планов на отдельные репозитории.

---

## Связанные файлы

- `server/src/main/java/com/satellite/app/model/Satellite.java`
- `server/src/main/java/com/satellite/app/EnergySystem.java`
- `server/src/main/java/com/satellite/app/SatelliteState.java`
- `server/src/main/resources/db/migration/V1__init_schema.sql`
