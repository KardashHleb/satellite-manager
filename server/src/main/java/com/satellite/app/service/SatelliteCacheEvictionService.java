package com.satellite.app.service;

import com.satellite.app.config.CacheConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Single place for cache invalidation.
 * <p>
 * Inside a transaction the evictions are deferred until after commit, so a concurrent reader cannot
 * put the old row back into the cache before the change is visible in the database, and nothing is
 * evicted when the transaction rolls back. Outside a transaction the evictions run immediately.
 * Cache errors (for example Redis is down) are only logged, the business operation is not affected.
 */
@Service
public class SatelliteCacheEvictionService {

    private static final Logger log = LoggerFactory.getLogger(SatelliteCacheEvictionService.class);

    private final CacheManager cacheManager;

    public SatelliteCacheEvictionService(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    /** Evicts only {@code satellite::{id}} (used for frequent telemetry updates). */
    public void evictSatellite(Long satelliteId) {
        runAfterCommit(() -> evict(CacheConfig.SATELLITE, satelliteId));
    }

    /**
     * A single satellite was created, changed or removed: evicts {@code satellite::{id}},
     * {@code satellites::all} and {@code constellation::{name}}.
     */
    public void satelliteChanged(Long satelliteId, String constellationName) {
        List<Long> ids = new ArrayList<>();
        if (satelliteId != null) {
            ids.add(satelliteId);
        }
        constellationChanged(constellationName, ids);
    }

    /**
     * A constellation or several of its satellites were changed: evicts {@code constellation::{name}},
     * {@code satellites::all} and {@code satellite::{id}} of every given satellite.
     */
    public void constellationChanged(String constellationName, Collection<Long> satelliteIds) {
        List<Long> ids = satelliteIds.stream().filter(Objects::nonNull).toList();
        runAfterCommit(() -> {
            if (constellationName != null) {
                evict(CacheConfig.CONSTELLATION, constellationName);
            }
            evict(CacheConfig.SATELLITES, CacheConfig.ALL_SATELLITES_KEY);
            for (Long id : ids) {
                evict(CacheConfig.SATELLITE, id);
            }
        });
    }

    private void runAfterCommit(Runnable eviction) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    eviction.run();
                }
            });
        } else {
            eviction.run();
        }
    }

    private void evict(String cacheName, Object key) {
        if (key == null) {
            return;
        }
        try {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.evict(key);
            }
        } catch (RuntimeException e) {
            log.warn("Cache evict failed for cache={}, key={}: {}", cacheName, key, e.getMessage());
        }
    }
}
