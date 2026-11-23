package com.satellite.app.service;

import com.satellite.app.config.CacheConfig;
import com.satellite.app.config.ResilientTransactionAwareCacheManager;
import com.satellite.app.dto.MissionRequest;
import com.satellite.app.dto.MissionTargetType;
import com.satellite.app.dto.crud.RenameConstellationRequest;
import com.satellite.app.dto.crud.UpdateSatelliteStateRequest;
import com.satellite.app.model.CommunicationSatellite;
import com.satellite.app.model.CommunicationSatelliteParam;
import com.satellite.app.model.ImagingSatellite;
import com.satellite.app.model.ImagingSatelliteParam;
import com.satellite.app.model.Satellite;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.transaction.TransactionAwareCacheDecorator;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Checks cache invalidation with an in-memory cache ({@code spring.cache.type=simple}), so no Redis is needed.
 * The in-memory cache manager is wrapped exactly like the Redis one in production.
 */
@SpringBootTest(properties = "spring.cache.type=simple")
@ActiveProfiles("test")
@DisplayName("Cache eviction after writes")
class CacheEvictionIntegrationTest {

    @Autowired
    private CrudManagementService crudService;

    @Autowired
    private SpaceOperationCenterService spaceCenter;

    @Autowired
    private ConstellationService constellationService;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @MockitoBean
    private TelemetryStreamClient telemetryStreamClient;

    private String constellationName;
    private Long commId;
    private Long imagingId;

    @BeforeEach
    void setUp() {
        constellationName = "C-" + UUID.randomUUID();
        spaceCenter.addSatellite(new AddSatelliteRequest(constellationName,
                new CommunicationSatelliteParam("Comm-1", 0.9, 100.0)));
        spaceCenter.addSatellite(new AddSatelliteRequest(constellationName,
                new ImagingSatelliteParam("Imager-1", 0.9, 1.0)));
        commId = spaceCenter.findSatellite(constellationName, "Comm-1").getId();
        imagingId = spaceCenter.findSatellite(constellationName, "Imager-1").getId();
    }

    private Cache cache(String name) {
        return cacheManager.getCache(name);
    }

    private void warmUpCaches() {
        crudService.getSatellite(commId);
        crudService.getSatellite(imagingId);
        crudService.getConstellation(constellationName);
        crudService.getAllSatellites();
        assertNotNull(cache(CacheConfig.SATELLITE).get(commId));
        assertNotNull(cache(CacheConfig.SATELLITE).get(imagingId));
        assertNotNull(cache(CacheConfig.CONSTELLATION).get(constellationName));
        assertNotNull(cache(CacheConfig.SATELLITES).get(CacheConfig.ALL_SATELLITES_KEY));
    }

    private void assertSatelliteEvicted(Long id) {
        assertNull(cache(CacheConfig.SATELLITE).get(id), "satellite::" + id + " must be evicted");
        assertNull(cache(CacheConfig.SATELLITES).get(CacheConfig.ALL_SATELLITES_KEY), "satellites::all must be evicted");
        assertNull(cache(CacheConfig.CONSTELLATION).get(constellationName), "constellation entry must be evicted");
    }

    private Satellite detached(String satelliteName) {
        return spaceCenter.findSatellite(constellationName, satelliteName);
    }

    // --- 1. Renaming and deleting a constellation ---

    @Test
    @DisplayName("renaming a constellation evicts the old name and the satellite list")
    void renameConstellation_evictsOldName() {
        warmUpCaches();
        String newName = constellationName + "-renamed";

        crudService.renameConstellation(constellationName, new RenameConstellationRequest(newName));

        assertNull(cache(CacheConfig.CONSTELLATION).get(constellationName));
        assertNull(cache(CacheConfig.SATELLITES).get(CacheConfig.ALL_SATELLITES_KEY));
        assertThrows(ResponseStatusException.class, () -> crudService.getConstellation(constellationName));
        assertEquals(newName, crudService.getConstellation(newName).getConstellationName());
    }

    @Test
    @DisplayName("deleting a constellation evicts the constellation, the list and its satellites")
    void deleteConstellation_evictsEverything() {
        warmUpCaches();

        crudService.deleteConstellation(constellationName);

        assertNull(cache(CacheConfig.CONSTELLATION).get(constellationName));
        assertNull(cache(CacheConfig.SATELLITES).get(CacheConfig.ALL_SATELLITES_KEY));
        assertNull(cache(CacheConfig.SATELLITE).get(commId));
        assertNull(cache(CacheConfig.SATELLITE).get(imagingId));
        assertThrows(ResponseStatusException.class, () -> crudService.getConstellation(constellationName));
        assertThrows(ResponseStatusException.class, () -> crudService.getSatellite(commId));
        assertTrue(crudService.getAllSatellites().stream().noneMatch(s -> s.getId().equals(commId)));
    }

    @Test
    @DisplayName("deleting a constellation through ConstellationService evicts it as well")
    void constellationServiceDelete_evictsEverything() {
        warmUpCaches();

        constellationService.delete(constellationName);

        assertSatelliteEvicted(commId);
        assertNull(cache(CacheConfig.SATELLITE).get(imagingId));
        assertThrows(ResponseStatusException.class, () -> crudService.getConstellation(constellationName));
    }

    // --- 2. Operations of SpaceOperationController / ConstellationService ---

    @Test
    @DisplayName("activating a satellite evicts satellite, list and constellation entries and is persisted")
    void activateSatellite_evictsAndPersists() {
        warmUpCaches();
        assertFalse(crudService.getSatellite(commId).isActive());

        assertTrue(spaceCenter.activateSatellite(constellationName, detached("Comm-1")));

        assertSatelliteEvicted(commId);
        assertTrue(crudService.getSatellite(commId).isActive());
        assertTrue(crudService.getConstellation(constellationName).getSatellites().stream()
                .filter(s -> s.getId().equals(commId)).allMatch(Satellite::isActive));
    }

    @Test
    @DisplayName("deactivating a satellite evicts its entries and is persisted")
    void deactivateSatellite_evictsAndPersists() {
        spaceCenter.activateSatellite(constellationName, detached("Comm-1"));
        warmUpCaches();
        assertTrue(crudService.getSatellite(commId).isActive());

        spaceCenter.deactivateSatellite(constellationName, detached("Comm-1"));

        assertSatelliteEvicted(commId);
        assertFalse(crudService.getSatellite(commId).isActive());
    }

    @Test
    @DisplayName("taking a photo is persisted and evicts the satellite")
    void takePhoto_evictsAndPersists() {
        spaceCenter.activateSatellite(constellationName, detached("Imager-1"));
        warmUpCaches();

        spaceCenter.takePhoto(constellationName, (ImagingSatellite) detached("Imager-1"));

        assertSatelliteEvicted(imagingId);
        assertEquals(1, ((ImagingSatellite) crudService.getSatellite(imagingId)).getPhotosTaken());
    }

    @Test
    @DisplayName("sending data is persisted and evicts the satellite")
    void sendData_evictsAndPersists() {
        spaceCenter.activateSatellite(constellationName, detached("Comm-1"));
        warmUpCaches();

        spaceCenter.sendData(constellationName, (CommunicationSatellite) detached("Comm-1"), 2.0);

        assertSatelliteEvicted(commId);
        assertEquals(2.0, ((CommunicationSatellite) crudService.getSatellite(commId)).getDataSent());
    }

    @Test
    @DisplayName("a single-satellite mission evicts that satellite")
    void singleSatelliteMission_evicts() {
        spaceCenter.activateSatellite(constellationName, detached("Imager-1"));
        warmUpCaches();

        spaceCenter.executeMission(new MissionRequest(MissionTargetType.SINGLE_SATELLITE, constellationName, "Imager-1"));

        assertSatelliteEvicted(imagingId);
        assertEquals(1, ((ImagingSatellite) crudService.getSatellite(imagingId)).getPhotosTaken());
    }

    @Test
    @DisplayName("a constellation mission evicts every satellite of the constellation")
    void constellationMission_evictsEverySatellite() {
        spaceCenter.activateAll(constellationName);
        warmUpCaches();

        spaceCenter.executeMission(new MissionRequest(MissionTargetType.CONSTELLATION, constellationName, null));

        assertSatelliteEvicted(commId);
        assertNull(cache(CacheConfig.SATELLITE).get(imagingId));
        assertEquals(1, ((ImagingSatellite) crudService.getSatellite(imagingId)).getPhotosTaken());
        assertTrue(((CommunicationSatellite) crudService.getSatellite(commId)).getDataSent() > 0);
    }

    @Test
    @DisplayName("activateAll evicts every satellite of the constellation")
    void activateAll_evictsEverySatellite() {
        warmUpCaches();

        spaceCenter.activateAll(constellationName);

        assertSatelliteEvicted(commId);
        assertNull(cache(CacheConfig.SATELLITE).get(imagingId));
        assertTrue(crudService.getSatellite(imagingId).isActive());
    }

    @Test
    @DisplayName("deactivateAll evicts every satellite of the constellation")
    void deactivateAll_evictsEverySatellite() {
        spaceCenter.activateAll(constellationName);
        warmUpCaches();

        spaceCenter.deactivateAll(constellationName);

        assertSatelliteEvicted(commId);
        assertNull(cache(CacheConfig.SATELLITE).get(imagingId));
        assertFalse(crudService.getSatellite(imagingId).isActive());
    }

    @Test
    @DisplayName("removing a satellite from its constellation evicts it")
    void removeSatellite_evicts() {
        warmUpCaches();

        constellationService.removeSatellite(constellationName, detached("Comm-1"));

        assertSatelliteEvicted(commId);
        assertThrows(ResponseStatusException.class, () -> crudService.getSatellite(commId));
        assertEquals(1, crudService.getConstellation(constellationName).getSatellites().size());
    }

    @Test
    @DisplayName("deleting a satellite also evicts its constellation")
    void deleteSatellite_evictsConstellation() {
        warmUpCaches();

        crudService.deleteSatellite(commId);

        assertSatelliteEvicted(commId);
        assertEquals(1, crudService.getConstellation(constellationName).getSatellites().size());
    }

    @Test
    @DisplayName("a state update through the CRUD API evicts the satellite and its constellation")
    void updateSatelliteState_evicts() {
        warmUpCaches();
        Long stateId = crudService.getSatellite(commId).getState().getId();

        crudService.updateSatelliteState(stateId, new UpdateSatelliteStateRequest(true));

        assertSatelliteEvicted(commId);
        assertTrue(crudService.getSatellite(commId).isActive());
    }

    // --- 3. findSatelliteByName is not cached ---

    @Test
    @DisplayName("findSatelliteByName always returns the current state")
    void findSatelliteByName_isNotStale() {
        assertFalse(crudService.findSatelliteByName(constellationName, "Comm-1").isActive());

        spaceCenter.activateSatellite(constellationName, detached("Comm-1"));
        assertTrue(crudService.findSatelliteByName(constellationName, "Comm-1").isActive());

        crudService.deleteSatellite(commId);
        assertThrows(ResponseStatusException.class,
                () -> crudService.findSatelliteByName(constellationName, "Comm-1"));
    }

    // --- 4. Transaction-aware cache manager ---

    @Test
    @DisplayName("the cache manager and its caches are transaction-aware")
    void cacheManager_isTransactionAware() {
        assertInstanceOf(ResilientTransactionAwareCacheManager.class, cacheManager);
        assertInstanceOf(TransactionAwareCacheDecorator.class, cache(CacheConfig.SATELLITE));
    }

    @Test
    @DisplayName("evictions run only after the transaction commits")
    void eviction_runsAfterCommit() {
        warmUpCaches();

        transactionTemplate.executeWithoutResult(status -> {
            spaceCenter.deactivateAll(constellationName);
            assertNotNull(cache(CacheConfig.CONSTELLATION).get(constellationName),
                    "entry must stay until commit");
        });

        assertNull(cache(CacheConfig.CONSTELLATION).get(constellationName));
    }

    @Test
    @DisplayName("nothing is evicted when the transaction rolls back")
    void eviction_skippedOnRollback() {
        warmUpCaches();

        transactionTemplate.executeWithoutResult(status -> {
            spaceCenter.activateAll(constellationName);
            status.setRollbackOnly();
        });

        assertNotNull(cache(CacheConfig.CONSTELLATION).get(constellationName));
        assertNotNull(cache(CacheConfig.SATELLITE).get(commId));
        assertFalse(crudService.getSatellite(commId).isActive());
    }

    @Test
    @DisplayName("a read by another request before the commit does not leave a stale entry")
    void concurrentReadBeforeCommit_doesNotLeaveStaleEntry() {
        transactionTemplate.executeWithoutResult(status -> {
            spaceCenter.activateSatellite(constellationName, detached("Comm-1"));
            // another request reads the satellite before this transaction commits and caches the old state
            Satellite seenByOtherRequest = CompletableFuture
                    .supplyAsync(() -> crudService.getSatellite(commId))
                    .join();
            assertFalse(seenByOtherRequest.isActive());
            assertNotNull(cache(CacheConfig.SATELLITE).get(commId));
        });

        assertNull(cache(CacheConfig.SATELLITE).get(commId), "the old state must be evicted after the commit");
        assertTrue(crudService.getSatellite(commId).isActive());
    }

    @Test
    @DisplayName("adding a satellite with invalid parameters does not leave an empty constellation")
    void addSatellite_isAtomic() {
        String name = "Atomic-" + UUID.randomUUID();

        assertThrows(IllegalArgumentException.class, () -> spaceCenter.addSatellite(
                new AddSatelliteRequest(name, new CommunicationSatelliteParam("Broken", 1.5, 10.0))));

        assertTrue(spaceCenter.get(name).isEmpty());
        assertTrue(crudService.listConstellations().stream()
                .noneMatch(c -> c.getConstellationName().equals(name)));
    }
}
