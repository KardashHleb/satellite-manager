package com.satellite.app.repository;

import com.satellite.app.model.SatelliteConstellation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unit tests for ConstellationRepository")
class ConstellationRepositoryUnitTest {

    // Test data constants
    private static final String EXISTING_CONSTELLATION_NAME = "Starlink";
    private static final String ANOTHER_EXISTING_NAME = "OneWeb";
    private static final String NON_EXISTING_NAME = "NonExistent";
    private static final String EMPTY_NAME = "";
    private static final String NEW_CONSTELLATION_NAME = "Kuiper";

    private ConstellationRepository repository;
    private SatelliteConstellation existingConstellation;

    @BeforeEach
    void setUp() {
        repository = new ConstellationRepository();
        // Use the single-argument constructor (name only)
        existingConstellation = new SatelliteConstellation(EXISTING_CONSTELLATION_NAME);
        repository.save(existingConstellation);
        repository.save(new SatelliteConstellation(ANOTHER_EXISTING_NAME));
    }

    @Test
    @DisplayName("Saving a new constellation adds it to the storage")
    void save_NewConstellation_ShouldAddToRepository() {
        // Arrange
        SatelliteConstellation newConstellation = new SatelliteConstellation(NEW_CONSTELLATION_NAME);

        // Act
        SatelliteConstellation result = repository.save(newConstellation);

        // Assert
        assertEquals(newConstellation, result);
        assertTrue(repository.existsByName(NEW_CONSTELLATION_NAME));
        assertEquals(3, repository.count());
    }

    @Test
    @DisplayName("Saving with an existing name overwrites the constellation")
    void save_ExistingName_ShouldOverwrite() {
        // Arrange
        SatelliteConstellation updated = new SatelliteConstellation(EXISTING_CONSTELLATION_NAME);

        // Act
        SatelliteConstellation result = repository.save(updated);

        // Assert
        assertEquals(EXISTING_CONSTELLATION_NAME, result.getConstellationName());
        assertEquals(2, repository.count()); // The count must not change

        // Check that the object was replaced
        Optional<SatelliteConstellation> found = repository.findByName(EXISTING_CONSTELLATION_NAME);
        assertTrue(found.isPresent());
        // This is a new object with the same name
        assertEquals(EXISTING_CONSTELLATION_NAME, found.get().getConstellationName());
    }

    @Test
    @DisplayName("Finding an existing constellation returns an Optional with the object")
    void findByName_ExistingConstellation_ShouldReturnOptionalWithValue() {
        // Act
        Optional<SatelliteConstellation> result = repository.findByName(EXISTING_CONSTELLATION_NAME);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(existingConstellation, result.get());
        assertEquals(EXISTING_CONSTELLATION_NAME, result.get().getConstellationName());
    }

    @Test
    @DisplayName("Finding a non-existent constellation returns an empty Optional")
    void findByName_NonExistingConstellation_ShouldReturnEmptyOptional() {
        // Act & Assert
        assertFalse(repository.findByName(NON_EXISTING_NAME).isPresent());
    }

    @Test
    @DisplayName("Getting all constellations returns a defensive copy")
    void findAll_ShouldReturnDefensiveCopy() {
        // Act
        Map<String, SatelliteConstellation> allConstellations = repository.findAll();

        // Modify the returned map
        allConstellations.put("Tampered", new SatelliteConstellation("Tampered"));

        // Assert - the original storage must not change
        assertFalse(repository.existsByName("Tampered"));
        assertEquals(2, repository.count());
    }

    @Test
    @DisplayName("Updating an existing constellation changes the data")
    void update_ExistingConstellation_ShouldUpdateData() {
        // Arrange
        SatelliteConstellation updated = new SatelliteConstellation(EXISTING_CONSTELLATION_NAME);

        // Act
        SatelliteConstellation result = repository.update(updated);

        // Assert
        assertEquals(EXISTING_CONSTELLATION_NAME, result.getConstellationName());

        // Check that the object was updated
        Optional<SatelliteConstellation> found = repository.findByName(EXISTING_CONSTELLATION_NAME);
        assertTrue(found.isPresent());
        // This is a new object after the update
        assertEquals(EXISTING_CONSTELLATION_NAME, found.get().getConstellationName());
    }

    @Test
    @DisplayName("Updating a non-existent constellation throws an exception")
    void update_NonExistingConstellation_ShouldThrowException() {
        // Arrange
        SatelliteConstellation nonExisting = new SatelliteConstellation(NON_EXISTING_NAME);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> repository.update(nonExisting));
    }

    @Test
    @DisplayName("Deleting an existing constellation removes it from the storage")
    void deleteByName_ExistingConstellation_ShouldRemoveIt() {
        // Arrange
        int initialCount = repository.count();

        // Act
        repository.deleteByName(EXISTING_CONSTELLATION_NAME);

        // Assert
        assertFalse(repository.existsByName(EXISTING_CONSTELLATION_NAME));
        assertEquals(initialCount - 1, repository.count());
    }

    @Test
    @DisplayName("Deleting a non-existent constellation does not affect the storage")
    void deleteByName_NonExistingConstellation_ShouldDoNothing() {
        // Arrange
        int initialCount = repository.count();

        // Act
        repository.deleteByName(NON_EXISTING_NAME);

        // Assert
        assertEquals(initialCount, repository.count());
    }

    @Test
    @DisplayName("Exists check correctly detects whether a constellation is present")
    void existsByName_ShouldCorrectlyIdentifyPresence() {
        // Act & Assert
        assertTrue(repository.existsByName(EXISTING_CONSTELLATION_NAME));
        assertFalse(repository.existsByName(NON_EXISTING_NAME));
    }

    @Test
    @DisplayName("Count returns the correct number of constellations")
    void count_ShouldReturnCorrectNumber() {
        // Act & Assert
        assertEquals(2, repository.count());

        // Add one more
        repository.save(new SatelliteConstellation("Third"));
        assertEquals(3, repository.count());
    }

    @Test
    @DisplayName("Saving null is handled correctly")
    void save_NullConstellation_ShouldThrowException() {
        assertThrows(NullPointerException.class, () -> repository.save(null));
    }

    @Test
    @DisplayName("Clear removes all constellations from the storage")
    void clear_ShouldRemoveAllConstellations() {
        // Act
        repository.clear();

        // Assert
        assertEquals(0, repository.count());
        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    @DisplayName("An empty name is handled correctly")
    void operations_WithEmptyName_ShouldWorkCorrectly() {
        // Arrange
        SatelliteConstellation emptyName = new SatelliteConstellation(EMPTY_NAME);

        // Act & Assert for save
        repository.save(emptyName);
        assertTrue(repository.existsByName(EMPTY_NAME));

        // Act & Assert for findByName
        assertTrue(repository.findByName(EMPTY_NAME).isPresent());

        // Act & Assert for deleteByName
        repository.deleteByName(EMPTY_NAME);
        assertFalse(repository.existsByName(EMPTY_NAME));
    }
}