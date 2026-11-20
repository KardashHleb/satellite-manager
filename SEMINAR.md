# Seminar 13 — Read caching with Redis

| | |
|---|---|
| Branch | `seminar_13` (merged into `main`) |
| Based on | [`seminar_12`](https://github.com/KardashHleb/satellite-manager/tree/seminar_12) |
| Last commit | 2026-06-14 |
| All seminars | [SEMINARS.md on main](https://github.com/KardashHleb/satellite-manager/blob/main/SEMINARS.md) |

## Learning goal

Reduce database load for frequent reads with a **distributed cache**: use **Spring Cache** with **Redis**, choose
time-to-live values, invalidate entries on writes, and keep the application working when the cache is down.

## What was done

- `spring-boot-starter-cache` and `spring-boot-starter-data-redis`, `@EnableCaching`.
- **`config/CacheConfig`**: a `RedisCacheManager` with JSON values, no caching of `null`, and per-cache TTLs;
  a `CacheErrorHandler` that only logs Redis errors (graceful degradation).
- **Cached reads** in `CrudManagementService`:

  | Method | Redis key | TTL |
  |---|---|---|
  | `getSatellite(id)` | `satellite::{id}` | 10 min |
  | `getConstellation(name)` | `constellation::{name}` | 15 min |
  | `getAllSatellites()` (new `GET /api/crud/satellites`) | `satellites::all` | 5 min |

- **Invalidation** with `@CacheEvict`: on satellite create and delete, battery and state updates, and when
  `TelemetryStreamClient` stores new temperatures (`SatelliteCacheEvictionService`).
- Actuator exposes `health`, `caches` and `metrics` (cache hit/miss metrics).
- Startup fixes from seminar 12: Flyway `baseline-on-migrate` in both services, and `telemetry-service` waits for a
  healthy `server`.
- Compose: `redis:7-alpine` with a health check. Tests run without Redis (`cache.type: none`).

## How to run

```bash
docker compose up --build
```

- `GET http://localhost:8082/api/crud/satellites` — cached list
- `GET http://localhost:8082/actuator/caches` — registered caches
- `GET http://localhost:8082/actuator/metrics/cache.gets` — hits and misses
- `docker exec satellite-redis redis-cli KEYS '*'` — keys in Redis

## Known issues

- Not every write invalidates the cache: renaming or deleting a constellation does not evict
  `constellation::{name}` / `satellites::all`, the operations of `/api/constellations/...` (activate, photo,
  send data, missions) do not evict the changed satellite, and entries cached by `findSatelliteByName` are never
  evicted. Cached data can be stale until the TTL expires.
- JPA entities are cached directly; DTOs would make the cached format independent of lazy associations.
- `gradle-wrapper.jar` was missing from Git, which broke `./gradlew` and `docker compose up --build` on a fresh
  clone; **fixed in the commit that added this file**.
