package com.satellite.app.repository;

import com.satellite.app.model.CommunicationSatellite;
import com.satellite.app.model.ImagingSatellite;
import com.satellite.app.model.SatelliteConstellation;
import com.satellite.app.service.ConstellationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

// Starts the real Spring context; the interactive console menu is switched off
@SpringBootTest(properties = "satellite.console.enabled=false")
@DisplayName("Integration tests for ConstellationRepository in the Spring context")
class ConstellationRepositoryIntegrationTest {

    @Autowired
    private ConstellationRepository repository;

    @Autowired
    private ConstellationService constellationService;

    private static final String TEST_CONSTELLATION_NAME = "IntegrationTestGroup";
    private static final String COMMUNICATION_SATELLITE_NAME = "ComSat-Integration";
    private static final String IMAGING_SATELLITE_NAME = "ImgSat-Integration";

    private CommunicationSatellite communicationSatellite;
    private ImagingSatellite imagingSatellite;

    @BeforeEach
    void setUp() {
        // The repository is a singleton bean shared by all tests, so start each test with an empty one
        repository.clear();

        communicationSatellite = new CommunicationSatellite(COMMUNICATION_SATELLITE_NAME, 0.85, 500.0);
        imagingSatellite = new ImagingSatellite(IMAGING_SATELLITE_NAME, 0.75, 2.5);
    }

    @Test
    @DisplayName("The repository and the service are injected by Spring and share the same storage")
    void beansAreInjectedThroughDependencyInjection() {
        assertNotNull(repository, "The repository must be injected");
        assertNotNull(constellationService, "The service must be injected");

        constellationService.create(TEST_CONSTELLATION_NAME);

        assertTrue(repository.existsByName(TEST_CONSTELLATION_NAME),
                "A constellation created through the service must be visible in the injected repository");
    }

    @Test
    @DisplayName("Test 1: Full constellation lifecycle through the repository")
    void fullConstellationLifecycleTest() {
        // 1. Create the constellation
        SatelliteConstellation constellation = new SatelliteConstellation(TEST_CONSTELLATION_NAME);
        assertEquals(TEST_CONSTELLATION_NAME, constellation.getConstellationName(),
                "The constellation name must match the given one");

        // 2. Add satellites of different types
        constellation.addSatellite(communicationSatellite);
        constellation.addSatellite(imagingSatellite);
        assertEquals(2, constellation.getSatellites().size(),
                "The constellation must contain 2 satellites");

        // 3. Save to the repository
        SatelliteConstellation savedConstellation = repository.save(constellation);
        assertNotNull(savedConstellation, "The saved constellation must not be null");
        assertTrue(repository.existsByName(TEST_CONSTELLATION_NAME),
                "The constellation must exist after saving");

        // 4. Retrieve from the repository
        Optional<SatelliteConstellation> retrievedOpt = repository.findByName(TEST_CONSTELLATION_NAME);
        assertTrue(retrievedOpt.isPresent(), "The constellation must be found by name");

        SatelliteConstellation retrievedConstellation = retrievedOpt.get();
        assertEquals(2, retrievedConstellation.getSatellites().size(),
                "The retrieved constellation must contain 2 satellites");

        // 5. Activate the satellites
        retrievedConstellation.activateAllSatellites();
        assertTrue(retrievedConstellation.getSatellites().get(0).isActive(),
                "The first satellite must be active after activation");
        assertTrue(retrievedConstellation.getSatellites().get(1).isActive(),
                "The second satellite must be active after activation");

        // 6. Update in the repository
        SatelliteConstellation updatedConstellation = repository.update(retrievedConstellation);
        assertEquals(2, updatedConstellation.getActiveSatelliteCount(),
                "There must be 2 active satellites after the update");

        // 7. Execute missions and check the changes
        double initialBattery1 = updatedConstellation.getSatellites().get(0).getBatteryLevel();
        double initialBattery2 = updatedConstellation.getSatellites().get(1).getBatteryLevel();

        updatedConstellation.executeAllMissions();

        double finalBattery1 = updatedConstellation.getSatellites().get(0).getBatteryLevel();
        double finalBattery2 = updatedConstellation.getSatellites().get(1).getBatteryLevel();

        assertTrue(finalBattery1 < initialBattery1,
                "The battery level of the first satellite must decrease after the mission");
        assertTrue(finalBattery2 < initialBattery2,
                "The battery level of the second satellite must decrease after the mission");

        // 8. Final update and check that the data is kept
        repository.update(updatedConstellation);
        Optional<SatelliteConstellation> finalRetrieved = repository.findByName(TEST_CONSTELLATION_NAME);

        assertTrue(finalRetrieved.isPresent(), "The constellation must be kept until the end of the test");
        assertEquals(2, finalRetrieved.get().getSatellites().size(),
                "The final state must contain 2 satellites");

        // 9. Check the constellation count
        assertEquals(1, repository.count(), "There must be one constellation in the repository");
    }

    @Test
    @DisplayName("Full lifecycle through the service: create, add satellites, activate, execute missions")
    void fullLifecycleThroughServiceTest() {
        constellationService.create(TEST_CONSTELLATION_NAME);
        constellationService.addSatellite(TEST_CONSTELLATION_NAME, communicationSatellite);
        constellationService.addSatellite(TEST_CONSTELLATION_NAME, imagingSatellite);

        SatelliteConstellation stored = repository.findByName(TEST_CONSTELLATION_NAME)
                .orElseThrow(() -> new AssertionError(TEST_CONSTELLATION_NAME + " not found"));
        assertEquals(2, stored.getSatellites().size(), "The stored constellation must contain 2 satellites");
        assertEquals(0, stored.getActiveSatelliteCount(), "New satellites must be inactive");

        constellationService.activateAll(TEST_CONSTELLATION_NAME);
        assertEquals(2, stored.getActiveSatelliteCount(), "Both satellites must be active after activation");

        constellationService.executeMissions(TEST_CONSTELLATION_NAME);
        assertTrue(communicationSatellite.getDataSent() > 0,
                "The communication satellite must have sent data");
        assertEquals(1, imagingSatellite.getPhotosTaken(), "The imaging satellite must have taken one photo");
    }

    @Test
    @DisplayName("Test 2: Several constellations and data isolation")
    void multipleConstellationsIsolationTest() {
        // Create the first constellation
        SatelliteConstellation group1 = new SatelliteConstellation("GroupAlpha");
        group1.addSatellite(new CommunicationSatellite("AlphaSat1", 0.9, 600.0));
        repository.save(group1);

        // Create the second constellation
        SatelliteConstellation group2 = new SatelliteConstellation("GroupBeta");
        group2.addSatellite(new ImagingSatellite("BetaSat1", 0.8, 1.5));
        group2.addSatellite(new ImagingSatellite("BetaSat2", 0.7, 2.0));
        repository.save(group2);

        // Check that both constellations are saved
        assertTrue(repository.existsByName("GroupAlpha"), "The first constellation must exist");
        assertTrue(repository.existsByName("GroupBeta"), "The second constellation must exist");

        // Get all constellations
        Map<String, SatelliteConstellation> allGroups = repository.findAll();
        assertEquals(2, allGroups.size(), "There must be 2 constellations in the repository");

        // Check that the data is isolated
        SatelliteConstellation retrievedGroup1 = repository.findByName("GroupAlpha")
                .orElseThrow(() -> new AssertionError("GroupAlpha not found"));
        SatelliteConstellation retrievedGroup2 = repository.findByName("GroupBeta")
                .orElseThrow(() -> new AssertionError("GroupBeta not found"));

        // Constellations must be independent
        assertEquals(1, retrievedGroup1.getSatellites().size(),
                "GroupAlpha must contain 1 satellite");
        assertEquals(2, retrievedGroup2.getSatellites().size(),
                "GroupBeta must contain 2 satellites");

        // Change one constellation
        retrievedGroup1.activateAllSatellites();
        repository.update(retrievedGroup1);

        // Check that the other constellation did not change
        SatelliteConstellation group2AfterUpdate = repository.findByName("GroupBeta")
                .orElseThrow(() -> new AssertionError("GroupBeta not found after the update"));

        assertFalse(group2AfterUpdate.getSatellites().get(0).isActive(),
                "GroupBeta satellites must not be activated when GroupAlpha changes");

        // Delete one constellation
        repository.deleteByName("GroupAlpha");

        // Check that the other one remains
        assertFalse(repository.existsByName("GroupAlpha"),
                "GroupAlpha must be deleted");
        assertTrue(repository.existsByName("GroupBeta"),
                "GroupBeta must remain after GroupAlpha is deleted");

        // Check the count
        assertEquals(1, repository.count(),
                "One constellation must remain after deleting the other");
    }

    @Test
    @DisplayName("Test 3: Updating a non-existent constellation throws an exception")
    void updateNonExistentConstellationThrowsException() {
        // Arrange
        SatelliteConstellation nonExistentConstellation = new SatelliteConstellation("NonExistent");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                repository.update(nonExistentConstellation)
        );
    }

    @Test
    @DisplayName("Test 4: Clearing the repository")
    void clearRepositoryTest() {
        // Arrange
        SatelliteConstellation constellation = new SatelliteConstellation("TestGroup");
        repository.save(constellation);
        assertTrue(repository.existsByName("TestGroup"));

        // Act
        repository.clear();

        // Assert
        assertFalse(repository.existsByName("TestGroup"));
        assertEquals(0, repository.count());
    }

    @Test
    @DisplayName("Test 5: Getting all constellations returns a copy")
    void findAllReturnsCopyTest() {
        // Arrange
        SatelliteConstellation constellation = new SatelliteConstellation("TestGroup");
        repository.save(constellation);

        // Act
        Map<String, SatelliteConstellation> allGroups = repository.findAll();
        allGroups.put("NewKey", new SatelliteConstellation("NewGroup"));

        // Assert - the original repository must not change
        assertEquals(1, repository.count());
        assertFalse(repository.existsByName("NewKey"));
    }
}
