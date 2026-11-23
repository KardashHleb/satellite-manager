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

- JPA entities are cached directly; DTOs would make the cached format independent of lazy associations.
- Telemetry updates evict only `satellite::{id}`, so temperatures in `satellites::all` and `constellation::{name}`
  can be stale until the TTL expires (evicting them on every telemetry tick would make those caches useless).

## Fixed after the seminar

- `gradle-wrapper.jar` was missing from Git, which broke `./gradlew` and `docker compose up --build` on a fresh
  clone; it was added together with this file.
- Renaming or deleting a constellation did not evict `constellation::{name}`, `satellites::all` and the satellites of
  a deleted constellation; all writes now go through `SatelliteCacheEvictionService`, which evicts the satellite,
  `satellites::all` and the constellation.
- The operations of `/api/constellations/...` and `/api/missions` (activate, deactivate, photo, send data, missions,
  activate/deactivate all) changed satellites without evicting anything; `ConstellationService` now evicts after each one.
- `findSatelliteByName` was cached under a key that no write ever evicted; it is no longer cached.
- The cache manager was not transaction-aware: evictions ran before the commit (a concurrent read could put the old row
  back) and also on rollback. `CacheConfig` now wraps the cache manager in `ResilientTransactionAwareCacheManager`:
  puts and evictions inside a transaction run after commit, are skipped on rollback, and Redis errors there are only
  logged, so the application still works without Redis.
- The custom `RedisCacheManager` bean had `@ConditionalOnBean(RedisConnectionFactory)` and was never created, so the
  JSON serializer and the TTLs were not used; they are now applied to the cache manager built by Spring Boot, and
  cached entities can be read back (unknown JSON properties are ignored).
- Cache hit/miss metrics were always zero; `spring.cache.redis.enable-statistics` is now on.
- Operations on satellites loaded in an earlier transaction (activate, deactivate, photo, send data, mission, remove)
  were not saved; they are applied to the managed instance.
- Adding a satellite with a new constellation was not atomic; `SpaceOperationCenterService.addSatellite` is
  transactional. Deleting a constellation or removing a satellite now publishes the deleted events.
- `SpaceOperationController` answered every error with 400 and emoji strings; it now returns 404 for an unknown
  constellation or satellite, 400 for bad input, 500 otherwise, without emojis.
- `Satellite.activate()` on an already active satellite printed "battery level is low", and the console "activate by
  type" counted satellites that failed to activate; it now reports "already active" and counts only real activations.
- `bootRun` / `run` did not pass the console input to the menu; `standardInput = System.in` was added.
- Unused copies of `telemetry.proto` in `server` and `telemetry-service` removed, `apache/kafka` image pinned to
  3.9.0, the gRPC server starter aligned with the client version (3.0.0).
- Tests: cache eviction for every write path, transaction-aware behavior (commit, rollback, concurrent read),
  a start with the Redis cache manager and no Redis server, and a Redis JSON round trip.
- All text (docs, comments, messages, test names) translated to English.
