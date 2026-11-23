package com.satellite.app.service;

import com.satellite.app.config.CacheConfig;
import com.satellite.app.config.ResilientTransactionAwareCacheManager;
import com.satellite.app.dto.crud.RenameConstellationRequest;
import com.satellite.app.model.CommunicationSatelliteParam;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.cache.transaction.TransactionAwareCacheDecorator;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Starts the application with the real Redis cache manager but without a Redis server (nothing listens on the
 * configured port). Checks the production wiring of the cache manager and that reads and writes still work:
 * cache errors, also those of the evictions that run after commit, are only logged.
 */
@SpringBootTest(properties = {
        "spring.cache.type=redis",
        // replaces the exclusion of the Redis auto-configuration from application-test.yml
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.gson.GsonAutoConfiguration",
        "spring.data.redis.host=127.0.0.1",
        "spring.data.redis.port=1",
        "spring.data.redis.timeout=500ms"
})
@ActiveProfiles("test")
@DisplayName("Application works when Redis is unavailable")
class RedisUnavailableCacheTest {

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private CrudManagementService crudService;

    @Autowired
    private SpaceOperationCenterService spaceCenter;

    @MockitoBean
    private TelemetryStreamClient telemetryStreamClient;

    @Test
    @DisplayName("the Redis cache manager is wrapped into the transaction-aware one")
    void redisCacheManager_isTransactionAware() {
        ResilientTransactionAwareCacheManager manager =
                assertInstanceOf(ResilientTransactionAwareCacheManager.class, cacheManager);
        assertInstanceOf(RedisCacheManager.class, manager.getTargetCacheManager());
        TransactionAwareCacheDecorator cache =
                assertInstanceOf(TransactionAwareCacheDecorator.class, cacheManager.getCache(CacheConfig.SATELLITE));
        assertInstanceOf(RedisCache.class, cache.getTargetCache());
    }

    @Test
    @DisplayName("cached reads and writes with evictions succeed without Redis")
    void readsAndWrites_workWithoutRedis() {
        String name = "NoRedis-" + UUID.randomUUID();
        spaceCenter.addSatellite(new AddSatelliteRequest(name, new CommunicationSatelliteParam("Comm-1", 0.9, 100.0)));
        Long id = spaceCenter.findSatellite(name, "Comm-1").getId();

        assertEquals("Comm-1", crudService.getSatellite(id).getName());
        assertEquals(name, crudService.getConstellation(name).getConstellationName());
        assertFalse(crudService.getAllSatellites().isEmpty());

        assertTrue(spaceCenter.activateSatellite(name, spaceCenter.findSatellite(name, "Comm-1")));
        assertTrue(crudService.getSatellite(id).isActive());

        String newName = name + "-renamed";
        crudService.renameConstellation(name, new RenameConstellationRequest(newName));
        crudService.deleteConstellation(newName);
        assertTrue(spaceCenter.get(newName).isEmpty());
    }
}
