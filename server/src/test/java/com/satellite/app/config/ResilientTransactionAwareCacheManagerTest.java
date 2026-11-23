package com.satellite.app.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Unit tests for ResilientTransactionAwareCacheManager")
class ResilientTransactionAwareCacheManagerTest {

    private final CacheManager cacheManager =
            new ResilientTransactionAwareCacheManager(new ConcurrentMapCacheManager("satellite"));

    @AfterEach
    void clearSynchronization() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    /** Simulates the end of a transaction: returns the registered callbacks after running them. */
    private static List<TransactionSynchronization> finishTransaction(boolean committed) {
        List<TransactionSynchronization> synchronizations = TransactionSynchronizationManager.getSynchronizations();
        TransactionSynchronizationManager.clearSynchronization();
        for (TransactionSynchronization synchronization : synchronizations) {
            if (committed) {
                synchronization.afterCommit();
            }
            synchronization.afterCompletion(committed
                    ? TransactionSynchronization.STATUS_COMMITTED
                    : TransactionSynchronization.STATUS_ROLLED_BACK);
        }
        return synchronizations;
    }

    @Test
    @DisplayName("outside a transaction put and evict run immediately")
    void noTransaction_immediate() {
        Cache cache = cacheManager.getCache("satellite");
        cache.put(1L, "value");
        assertEquals("value", cache.get(1L).get());

        cache.evict(1L);
        assertNull(cache.get(1L));
    }

    @Test
    @DisplayName("inside a transaction put and evict run after commit")
    void transaction_deferredUntilCommit() {
        Cache cache = cacheManager.getCache("satellite");
        cache.put(1L, "old");

        TransactionSynchronizationManager.initSynchronization();
        cache.evict(1L);
        cache.put(2L, "new");
        assertEquals("old", cache.get(1L).get());
        assertNull(cache.get(2L));

        finishTransaction(true);

        assertNull(cache.get(1L));
        assertEquals("new", cache.get(2L).get());
    }

    @Test
    @DisplayName("nothing is written or evicted when the transaction rolls back")
    void rollback_noChanges() {
        Cache cache = cacheManager.getCache("satellite");
        cache.put(1L, "old");

        TransactionSynchronizationManager.initSynchronization();
        cache.evict(1L);
        cache.put(2L, "new");
        finishTransaction(false);

        assertEquals("old", cache.get(1L).get());
        assertNull(cache.get(2L));
    }

    @Test
    @DisplayName("cache errors after commit are logged and do not stop the other evictions")
    void errorAfterCommit_isLogged() {
        Cache failing = mock(Cache.class);
        when(failing.getName()).thenReturn("satellite");
        doThrow(new RedisConnectionFailureException("Redis is down")).when(failing).evict(any());
        doThrow(new RedisConnectionFailureException("Redis is down")).when(failing).put(any(), any());
        CacheManager target = mock(CacheManager.class);
        when(target.getCache("satellite")).thenReturn(failing);
        Cache cache = new ResilientTransactionAwareCacheManager(target).getCache("satellite");

        TransactionSynchronizationManager.initSynchronization();
        cache.evict(1L);
        cache.evict(2L);
        cache.put(3L, "value");
        assertDoesNotThrow(() -> finishTransaction(true));

        verify(failing).evict(1L);
        verify(failing).evict(2L);
        verify(failing).put(3L, "value");
        assertDoesNotThrow(() -> cache.evict(4L), "errors outside a transaction are logged too");
    }

    @Test
    @DisplayName("unknown caches are reported as null")
    void unknownCache_isNull() {
        CacheManager target = mock(CacheManager.class);
        assertNull(new ResilientTransactionAwareCacheManager(target).getCache("missing"));
    }
}
