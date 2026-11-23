package com.satellite.app.service;

import com.satellite.app.model.CommunicationSatellite;
import com.satellite.app.model.ImagingSatellite;
import com.satellite.app.model.Satellite;
import com.satellite.app.model.SatelliteConstellation;
import com.satellite.app.repository.ConstellationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit tests for ConstellationService")
class ConstellationServiceTest {

    private static final String CONSTELLATION_NAME = "Starlink";
    private static final String NON_EXISTENT_NAME = "NonExistent";

    @Mock
    private ConstellationRepository repository;

    @Mock
    private SatelliteConstellation constellation;

    @Mock
    private Satellite satellite;

    @Mock
    private ImagingSatellite imagingSatellite;

    @Mock
    private CommunicationSatellite communicationSatellite;

    @InjectMocks
    private ConstellationService service;

    @BeforeEach
    void setUp() {
        // Common mock setup
        lenient().when(constellation.getConstellationName()).thenReturn(CONSTELLATION_NAME);
    }

    @Test
    @DisplayName("create creates a new constellation for a unique name")
    void create_ShouldCreateNewConstellation_WhenNameIsUnique() {
        // Arrange
        when(repository.existsByName(CONSTELLATION_NAME)).thenReturn(false);
        when(repository.save(any(SatelliteConstellation.class))).thenReturn(constellation);

        // Act
        SatelliteConstellation result = service.create(CONSTELLATION_NAME);

        // Assert
        assertNotNull(result);
        verify(repository, times(1)).existsByName(CONSTELLATION_NAME);
        verify(repository, times(1)).save(any(SatelliteConstellation.class));
    }

    @Test
    @DisplayName("create throws an exception when the name already exists")
    void create_ShouldThrowException_WhenNameAlreadyExists() {
        // Arrange
        when(repository.existsByName(CONSTELLATION_NAME)).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.create(CONSTELLATION_NAME)
        );
        assertTrue(exception.getMessage().contains("already exists"));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("get returns the constellation for an existing name")
    void get_ShouldReturnConstellation_WhenNameExists() {
        // Arrange
        when(repository.findByName(CONSTELLATION_NAME)).thenReturn(Optional.of(constellation));

        // Act
        Optional<SatelliteConstellation> result = service.get(CONSTELLATION_NAME);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(constellation, result.get());
        verify(repository, times(1)).findByName(CONSTELLATION_NAME);
    }

    @Test
    @DisplayName("addSatellite adds a satellite to the constellation")
    void addSatellite_ShouldAddSatelliteToConstellation() {
        // Arrange
        when(repository.findByName(CONSTELLATION_NAME)).thenReturn(Optional.of(constellation));
        doNothing().when(constellation).addSatellite(satellite);

        // Act
        service.addSatellite(CONSTELLATION_NAME, satellite);

        // Assert
        verify(constellation, times(1)).addSatellite(satellite);
        verify(repository, times(1)).update(constellation);
    }

    @Test
    @DisplayName("executeMissions executes the missions of all satellites in the constellation")
    void executeMissions_ShouldExecuteAllMissionsInConstellation() {
        // Arrange
        when(repository.findByName(CONSTELLATION_NAME)).thenReturn(Optional.of(constellation));
        doNothing().when(constellation).executeAllMissions();

        // Act
        service.executeMissions(CONSTELLATION_NAME);

        // Assert
        verify(constellation, times(1)).executeAllMissions();
        verify(repository, times(1)).update(constellation);
    }


    @Test
    @DisplayName("takePhoto calls the photo method of the satellite")
    void takePhoto_ShouldCallTakePhotoOnImagingSatellite() {
        // Arrange
        when(repository.findByName(CONSTELLATION_NAME)).thenReturn(Optional.of(constellation));
        doNothing().when(imagingSatellite).takePhoto();

        // Act
        service.takePhoto(CONSTELLATION_NAME, imagingSatellite);

        // Assert
        verify(imagingSatellite, times(1)).takePhoto();
        verify(repository, times(1)).update(constellation);
    }

    @Test
    @DisplayName("getStatus returns the constellation status report")
    void getStatus_ShouldReturnStatusReport() {
        // Arrange
        String expectedStatus = "Status Report";
        when(repository.findByName(CONSTELLATION_NAME)).thenReturn(Optional.of(constellation));
        when(constellation.getStatusReport()).thenReturn(expectedStatus);

        // Act
        String result = service.getStatus(CONSTELLATION_NAME);

        // Assert
        assertEquals(expectedStatus, result);
        verify(constellation, times(1)).getStatusReport();
    }

    @Test
    @DisplayName("getOrThrow throws an exception when the constellation is missing")
    void getOrThrow_ShouldThrowException_WhenConstellationNotFound() {
        // Arrange
        when(repository.findByName(NON_EXISTENT_NAME)).thenReturn(Optional.empty());

        // Act & Assert (use reflection to test a private method)
        // In a real project you could use Spring Test or change the access modifier
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.getStatus(NON_EXISTENT_NAME)
        );
        assertTrue(exception.getMessage().contains("not found"));
    }

    @Test
    @DisplayName("getSatellitesByType returns satellites of the given type")
    void getSatellitesByType_ShouldReturnSatellitesOfSpecificType() {
        // Arrange
        List<ImagingSatellite> expectedSatellites = Arrays.asList(imagingSatellite);
        when(repository.findByName(CONSTELLATION_NAME)).thenReturn(Optional.of(constellation));
        when(constellation.getSatellitesByType(ImagingSatellite.class)).thenReturn(expectedSatellites);

        // Act
        List<ImagingSatellite> result = service.getSatellitesByType(CONSTELLATION_NAME, ImagingSatellite.class);

        // Assert
        assertEquals(expectedSatellites, result);
        verify(constellation, times(1)).getSatellitesByType(ImagingSatellite.class);
    }
}