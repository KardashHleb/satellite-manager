package com.satellite.app.service;

import com.satellite.app.config.CacheConfig;
import com.satellite.app.dto.crud.RenameConstellationRequest;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Checks cache invalidation with an in-memory cache ({@code spring.cache.type=simple}), so no Redis is needed.
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

    @Test
    @DisplayName("activating a satellite evicts satellite, list and constellation entries and is persisted")
    void activateSatellite_evictsAndPersists() {
        warmUpCaches();
        assertFalse(crudService.getSatellite(commId).isActive());

        Satellite detached = spaceCenter.findSatellite(constellationName, "Comm-1");
        spaceCenter.activateSatellite(constellationName, detached);

        assertNull(cache(CacheConfig.SATELLITE).get(commId));
        assertNull(cache(CacheConfig.SATELLITES).get(CacheConfig.ALL_SATELLITES_KEY));
        assertNull(cache(CacheConfig.CONSTELLATION).get(constellationName));
        assertTrue(crudService.getSatellite(commId).isActive());
    }

    @Test
    @DisplayName("taking a photo is persisted and evicts the satellite")
    void takePhoto_evictsAndPersists() {
        spaceCenter.activateSatellite(constellationName, spaceCenter.findSatellite(constellationName, "Imager-1"));
        warmUpCaches();

        ImagingSatellite detached = (ImagingSatellite) spaceCenter.findSatellite(constellationName, "Imager-1");
        spaceCenter.takePhoto(constellationName, detached);

        assertNull(cache(CacheConfig.SATELLITE).get(imagingId));
        assertEquals(1, ((ImagingSatellite) crudService.getSatellite(imagingId)).getPhotosTaken());
    }

    @Test
    @DisplayName("activateAll evicts every satellite of the constellation")
    void activateAll_evictsEverySatellite() {
        warmUpCaches();

        spaceCenter.activateAll(constellationName);

        assertNull(cache(CacheConfig.SATELLITE).get(commId));
        assertNull(cache(CacheConfig.SATELLITE).get(imagingId));
        assertNull(cache(CacheConfig.SATELLITES).get(CacheConfig.ALL_SATELLITES_KEY));
        assertNull(cache(CacheConfig.CONSTELLATION).get(constellationName));
        assertTrue(crudService.getSatellite(imagingId).isActive());
    }

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
        assertThrows(ResponseStatusException.class, () -> crudService.getSatellite(commId));
    }

    @Test
    @DisplayName("deleting a satellite also evicts its constellation")
    void deleteSatellite_evictsConstellation() {
        warmUpCaches();

        crudService.deleteSatellite(commId);

        assertNull(cache(CacheConfig.SATELLITE).get(commId));
        assertNull(cache(CacheConfig.CONSTELLATION).get(constellationName));
        assertEquals(1, crudService.getConstellation(constellationName).getSatellites().size());
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
