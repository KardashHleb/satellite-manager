package com.satellite.app.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.transaction.TransactionAwareCacheDecorator;
import org.springframework.cache.transaction.TransactionAwareCacheManagerProxy;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Transaction-aware wrapper around the real cache manager (Redis in production, an in-memory one in tests).
 * <p>
 * Like Spring's {@link TransactionAwareCacheManagerProxy}, {@code put}, {@code evict} and {@code clear} called
 * inside a transaction are executed only after the transaction commits, and not at all after a rollback.
 * So an eviction cannot happen before the new data is visible in the database (a concurrent reader would put
 * the old row back into the cache), and a rolled back change does not evict anything.
 * <p>
 * In addition, errors of the real cache in these operations are only logged. The deferred operations run after
 * the commit, outside the {@code CacheErrorHandler}; an exception there would fail a request whose data is
 * already committed and skip the remaining after-commit callbacks. With Redis down the application keeps working.
 */
public class ResilientTransactionAwareCacheManager extends TransactionAwareCacheManagerProxy {

    private static final Logger log = LoggerFactory.getLogger(ResilientTransactionAwareCacheManager.class);

    private final CacheManager targetCacheManager;

    public ResilientTransactionAwareCacheManager(CacheManager targetCacheManager) {
        super(targetCacheManager);
        this.targetCacheManager = targetCacheManager;
    }

    public CacheManager getTargetCacheManager() {
        return targetCacheManager;
    }

    @Override
    public Cache getCache(String name) {
        Cache targetCache = targetCacheManager.getCache(name);
        return targetCache != null ? new ResilientTransactionAwareCache(targetCache) : null;
    }

    /**
     * Extends {@link TransactionAwareCacheDecorator}, so tools that unwrap that type (for example the cache
     * metrics of Spring Boot Actuator) still see the real cache.
     */
    static class ResilientTransactionAwareCache extends TransactionAwareCacheDecorator {

        ResilientTransactionAwareCache(Cache targetCache) {
            super(targetCache);
        }

        @Override
        public void put(Object key, Object value) {
            runAfterCommit("put", key, () -> getTargetCache().put(key, value));
        }

        @Override
        public void evict(Object key) {
            runAfterCommit("evict", key, () -> getTargetCache().evict(key));
        }

        @Override
        public void clear() {
            runAfterCommit("clear", "*", () -> getTargetCache().clear());
        }

        private void runAfterCommit(String operation, Object key, Runnable action) {
            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        runLoggingErrors(operation, key, action);
                    }
                });
            } else {
                runLoggingErrors(operation, key, action);
            }
        }

        private void runLoggingErrors(String operation, Object key, Runnable action) {
            try {
                action.run();
            } catch (RuntimeException e) {
                log.warn("Cache {} failed for cache={}, key={}: {}", operation, getName(), key, e.getMessage());
            }
        }
    }
}
