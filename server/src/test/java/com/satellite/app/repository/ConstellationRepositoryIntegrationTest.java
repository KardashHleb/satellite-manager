package com.satellite.app.repository;

import com.satellite.app.model.CommunicationSatellite;
import com.satellite.app.model.ImagingSatellite;
import com.satellite.app.model.SatelliteConstellation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
@DisplayName("ConstellationRepository integration test with mocks")
class ConstellationRepositoryIntegrationTest {

    @InjectMocks
    private ConstellationRepository repository;

    // Mock for the internal storage (to test the business logic)
    private Map<String, SatelliteConstellation> mockConstellations;

    private static final String TEST_CONSTELLATION_NAME = "IntegrationTestGroup";
    private static final String COMMUNICATION_SATELLITE_NAME = "ComSat-Integration";
    private static final String IMAGING_SATELLITE_NAME = "ImgSat-Integration";

    private CommunicationSatellite communicationSatellite;
    private ImagingSatellite imagingSatellite;

    @BeforeEach
    void setUp() {
        // Create real objects for the test
        communicationSatellite = new CommunicationSatellite(COMMUNICATION_SATELLITE_NAME, 0.85, 500.0);
        imagingSatellite = new ImagingSatellite(IMAGING_SATELLITE_NAME, 0.75, 2.5);

        // Initialize the mock storage
        mockConstellations = new HashMap<>();

        // Use reflection to replace the internal storage
        try {
            var field = ConstellationRepository.class.getDeclaredField("constellations");
            field.setAccessible(true);
            field.set(repository, mockConstellations);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set up test", e);
        }
    }

    @Test
    @DisplayName("Test 1: full life cycle of a satellite constellation through the repository")
    void fullConstellationLifecycleTest() {
        // 1. Create the constellation
        SatelliteConstellation constellation = new SatelliteConstellation(TEST_CONSTELLATION_NAME);
        assertNotNull(constellation, "The constellation should be created");
        assertEquals(TEST_CONSTELLATION_NAME, constellation.getConstellationName(),
                "The constellation name should match the given one");

        // 2. Add satellites of different types
        constellation.addSatellite(communicationSatellite);
        constellation.addSatellite(imagingSatellite);
        assertEquals(2, constellation.getSatellites().size(),
                "The constellation should have 2 satellites");

        // 3. Save to the repository
        SatelliteConstellation savedConstellation = repository.save(constellation);
        assertNotNull(savedConstellation, "The saved constellation should not be null");
        assertTrue(repository.existsByName(TEST_CONSTELLATION_NAME),
                "The constellation should exist after saving");

        // 4. Load from the repository
        Optional<SatelliteConstellation> retrievedOpt = repository.findByName(TEST_CONSTELLATION_NAME);
        assertTrue(retrievedOpt.isPresent(), "The constellation should be found by name");

        SatelliteConstellation retrievedConstellation = retrievedOpt.get();
        assertEquals(2, retrievedConstellation.getSatellites().size(),
                "The loaded constellation should contain 2 satellites");

        // 5. Change the satellite state
        retrievedConstellation.activateAllSatellites();
        assertTrue(retrievedConstellation.getSatellites().get(0).isActive(),
                "The first satellite should be active after activation");
        assertTrue(retrievedConstellation.getSatellites().get(1).isActive(),
                "The second satellite should be active after activation");

        // 6. Update in the repository
        SatelliteConstellation updatedConstellation = repository.update(retrievedConstellation);
        assertEquals(2, updatedConstellation.getActiveSatelliteCount(),
                "There should be 2 active satellites after the update");

        // 7. Run missions and check the changes
        double initialBattery1 = updatedConstellation.getSatellites().get(0).getBatteryLevel();
        double initialBattery2 = updatedConstellation.getSatellites().get(1).getBatteryLevel();

        updatedConstellation.executeAllMissions();

        double finalBattery1 = updatedConstellation.getSatellites().get(0).getBatteryLevel();
        double finalBattery2 = updatedConstellation.getSatellites().get(1).getBatteryLevel();

        assertTrue(finalBattery1 < initialBattery1,
                "The battery level of the first satellite should drop after the mission");
        assertTrue(finalBattery2 < initialBattery2,
                "The battery level of the second satellite should drop after the mission");

        // 8. Final update and data check
        SatelliteConstellation finalUpdated = repository.update(updatedConstellation);
        Optional<SatelliteConstellation> finalRetrieved = repository.findByName(TEST_CONSTELLATION_NAME);

        assertTrue(finalRetrieved.isPresent(), "The constellation should be kept until the end of the test");
        assertEquals(2, finalRetrieved.get().getSatellites().size(),
                "The final state should contain 2 satellites");

        // 9. Check the constellation count
        assertEquals(1, repository.count(), "There should be one constellation in the repository");
    }

    @Test
    @DisplayName("Test 2: several constellations and data isolation")
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
        assertTrue(repository.existsByName("GroupAlpha"), "The first constellation should exist");
        assertTrue(repository.existsByName("GroupBeta"), "The second constellation should exist");

        // Get all constellations
        Map<String, SatelliteConstellation> allGroups = repository.findAll();
        assertEquals(2, allGroups.size(), "There should be 2 constellations in the repository");

        // Check data isolation
        SatelliteConstellation retrievedGroup1 = repository.findByName("GroupAlpha")
                .orElseThrow(() -> new AssertionError("GroupAlpha not found"));
        SatelliteConstellation retrievedGroup2 = repository.findByName("GroupBeta")
                .orElseThrow(() -> new AssertionError("GroupBeta not found"));

        // The constellations should be independent
        assertEquals(1, retrievedGroup1.getSatellites().size(),
                "GroupAlpha should contain 1 satellite");
        assertEquals(2, retrievedGroup2.getSatellites().size(),
                "GroupBeta should contain 2 satellites");

        // Change one constellation
        retrievedGroup1.activateAllSatellites();
        repository.update(retrievedGroup1);

        // Check that the other constellation did not change
        SatelliteConstellation group2AfterUpdate = repository.findByName("GroupBeta")
                .orElseThrow(() -> new AssertionError("GroupBeta not found after the update"));

        assertFalse(group2AfterUpdate.getSatellites().get(0).isActive(),
                "GroupBeta satellites should not be activated when GroupAlpha changes");

        // Delete one constellation
        repository.deleteByName("GroupAlpha");

        // Check that the other one is still there
        assertFalse(repository.existsByName("GroupAlpha"),
                "GroupAlpha should be deleted");
        assertTrue(repository.existsByName("GroupBeta"),
                "GroupBeta should remain after GroupAlpha is deleted");

        // Check the count
        assertEquals(1, repository.count(),
                "One constellation should remain after deleting the other");
    }

    @Test
    @DisplayName("Test 3: updating a missing constellation throws an exception")
    void updateNonExistentConstellationThrowsException() {
        // Arrange
        SatelliteConstellation nonExistentConstellation = new SatelliteConstellation("NonExistent");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                repository.update(nonExistentConstellation)
        );
    }

    @Test
    @DisplayName("Test 4: clearing the repository")
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
    @DisplayName("Test 5: getting all constellations returns a copy")
    void findAllReturnsCopyTest() {
        // Arrange
        SatelliteConstellation constellation = new SatelliteConstellation("TestGroup");
        repository.save(constellation);

        // Act
        Map<String, SatelliteConstellation> allGroups = repository.findAll();
        allGroups.put("NewKey", new SatelliteConstellation("NewGroup"));

        // Assert - the original repository should not change
        assertEquals(1, repository.count());
        assertFalse(repository.existsByName("NewGroup"));
    }
}