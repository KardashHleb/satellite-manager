package com.satellite.app.service;

import com.satellite.app.config.CacheConfig;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Single place for cache invalidation after writes.
 * <p>
 * The cache manager is transaction-aware ({@link com.satellite.app.config.ResilientTransactionAwareCacheManager}):
 * evictions requested inside a transaction run after it commits, so a concurrent reader cannot put the old row
 * back into the cache before the change is visible in the database, and nothing is evicted when the transaction
 * rolls back. Outside a transaction the evictions run immediately. Cache errors are only logged.
 */
@Service
public class SatelliteCacheEvictionService {

    private final CacheManager cacheManager;

    public SatelliteCacheEvictionService(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    /** Evicts only {@code satellite::{id}} (used for frequent telemetry updates). */
    public void evictSatellite(Long satelliteId) {
        evict(CacheConfig.SATELLITE, satelliteId);
    }

    /**
     * A single satellite was created, changed or removed: evicts {@code satellite::{id}},
     * {@code satellites::all} and {@code constellation::{name}}.
     */
    public void satelliteChanged(Long satelliteId, String constellationName) {
        constellationChanged(constellationName, satelliteId != null ? List.of(satelliteId) : List.of());
    }

    /**
     * A constellation or several of its satellites were changed (also renamed or deleted): evicts
     * {@code constellation::{name}}, {@code satellites::all} and {@code satellite::{id}} of every given satellite.
     */
    public void constellationChanged(String constellationName, Collection<Long> satelliteIds) {
        evict(CacheConfig.CONSTELLATION, constellationName);
        evict(CacheConfig.SATELLITES, CacheConfig.ALL_SATELLITES_KEY);
        satelliteIds.stream()
                .filter(Objects::nonNull)
                .forEach(id -> evict(CacheConfig.SATELLITE, id));
    }

    private void evict(String cacheName, Object key) {
        if (key == null) {
            return;
        }
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.evict(key);
        }
    }
}
