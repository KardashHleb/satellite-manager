package com.satellite.app.repository;

import com.satellite.app.model.SatelliteConstellation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Mock tests for ConstellationRepository")
class ConstellationRepositoryMockTest {

    @Mock
    private SatelliteConstellation mockConstellation;

    @InjectMocks
    private ConstellationRepository repository;

    private Map<String, SatelliteConstellation> testMap;

    @BeforeEach
    void setUp() {
        // Use reflection to replace the internal Map
        testMap = new HashMap<>();
        try {
            var field = ConstellationRepository.class.getDeclaredField("constellations");
            field.setAccessible(true);
            field.set(repository, testMap);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set up test", e);
        }
    }

    @Test
    @DisplayName("Test 1: saving a new constellation adds it to the storage")
    void saveNewConstellationSuccessfully() {
        // Arrange
        when(mockConstellation.getConstellationName()).thenReturn("TestGroup");

        // Act
        SatelliteConstellation result = repository.save(mockConstellation);

        // Assert
        assertEquals(mockConstellation, result);
        assertEquals(1, testMap.size());
        assertTrue(testMap.containsKey("TestGroup"));
        assertSame(mockConstellation, testMap.get("TestGroup"));
    }

    @Test
    @DisplayName("Test 2: finding an existing constellation returns an Optional with the object")
    void findExistingConstellationReturnsObject() {
        // Arrange
        when(mockConstellation.getConstellationName()).thenReturn("ExistingGroup");
        repository.save(mockConstellation);

        // Act
        Optional<SatelliteConstellation> result = repository.findByName("ExistingGroup");

        // Assert
        assertTrue(result.isPresent());
        assertEquals(mockConstellation, result.get());
        verify(mockConstellation, atLeastOnce()).getConstellationName();
    }

    @Test
    @DisplayName("Test 3: finding a missing constellation returns an empty Optional")
    void findNonExistingConstellationReturnsEmpty() {
        // Act
        Optional<SatelliteConstellation> result = repository.findByName("NonExisting");

        // Assert
        assertFalse(result.isPresent());
        assertEquals(0, testMap.size());
    }

    @Test
    @DisplayName("Test 4: deleting an existing constellation removes it from the storage")
    void deleteExistingConstellationRemovesIt() {
        // Arrange
        when(mockConstellation.getConstellationName()).thenReturn("GroupToDelete");
        repository.save(mockConstellation);
        assertEquals(1, testMap.size());

        // Act
        repository.deleteByName("GroupToDelete");

        // Assert
        assertEquals(0, testMap.size());
        assertFalse(testMap.containsKey("GroupToDelete"));
    }

    @Test
    @DisplayName("Test 5: the existence check returns the correct result")
    void existsByNameReturnsCorrectResult() {
        // Arrange
        when(mockConstellation.getConstellationName()).thenReturn("Existing");
        repository.save(mockConstellation);

        // Act & Assert for an existing one
        assertTrue(repository.existsByName("Existing"));

        // Act & Assert for a missing one
        assertFalse(repository.existsByName("NonExisting"));
    }
}