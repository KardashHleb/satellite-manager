package com.satellite.app.service;

import com.satellite.app.exception.ResourceNotFoundException;
import com.satellite.app.model.CommunicationSatellite;
import com.satellite.app.model.ImagingSatellite;
import com.satellite.app.model.Satellite;
import com.satellite.app.model.SatelliteConstellation;
import com.satellite.app.kafka.SatelliteEventPublisher;
import com.satellite.app.repository.ConstellationRepository;
import com.satellite.app.repository.SatelliteRepository;
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
    private SatelliteRepository satelliteRepository;

    @Mock
    private SatelliteEventPublisher satelliteEventPublisher;

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
        lenient().when(constellation.getConstellationName()).thenReturn(CONSTELLATION_NAME);
    }

    @Test
    @DisplayName("create creates a new constellation when the name is unique")
    void create_ShouldCreateNewConstellation_WhenNameIsUnique() {
        when(repository.existsByConstellationName(CONSTELLATION_NAME)).thenReturn(false);
        when(repository.save(any(SatelliteConstellation.class))).thenReturn(constellation);

        SatelliteConstellation result = service.create(CONSTELLATION_NAME);

        assertNotNull(result);
        verify(repository, times(1)).existsByConstellationName(CONSTELLATION_NAME);
        verify(repository, times(1)).save(any(SatelliteConstellation.class));
    }

    @Test
    @DisplayName("create throws an exception when the name already exists")
    void create_ShouldThrowException_WhenNameAlreadyExists() {
        when(repository.existsByConstellationName(CONSTELLATION_NAME)).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.create(CONSTELLATION_NAME)
        );
        assertTrue(exception.getMessage().contains("already exists"));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("get returns the constellation when the name exists")
    void get_ShouldReturnConstellation_WhenNameExists() {
        when(repository.findByConstellationName(CONSTELLATION_NAME)).thenReturn(Optional.of(constellation));

        Optional<SatelliteConstellation> result = service.get(CONSTELLATION_NAME);

        assertTrue(result.isPresent());
        assertEquals(constellation, result.get());
        verify(repository, times(1)).findByConstellationName(CONSTELLATION_NAME);
    }

    @Test
    @DisplayName("addSatellite adds the satellite and publishes a created event")
    void addSatellite_ShouldAddSatelliteToConstellation() {
        when(repository.findByConstellationName(CONSTELLATION_NAME)).thenReturn(Optional.of(constellation));
        doNothing().when(constellation).addSatellite(satellite);
        when(satellite.getName()).thenReturn("TestSat");
        when(satelliteRepository.findByConstellation_ConstellationNameAndName(CONSTELLATION_NAME, "TestSat"))
                .thenReturn(Optional.of(satellite));

        service.addSatellite(CONSTELLATION_NAME, satellite);

        verify(constellation, times(1)).addSatellite(satellite);
        verify(repository, times(1)).saveAndFlush(constellation);
        verify(satelliteEventPublisher, times(1)).publishCreated(satellite);
    }

    @Test
    @DisplayName("executeMissions executes the missions of all satellites")
    void executeMissions_ShouldExecuteAllMissionsInConstellation() {
        when(repository.findByConstellationName(CONSTELLATION_NAME)).thenReturn(Optional.of(constellation));
        doNothing().when(constellation).executeAllMissions();

        service.executeMissions(CONSTELLATION_NAME);

        verify(constellation, times(1)).executeAllMissions();
        verify(repository, times(1)).save(constellation);
    }

    @Test
    @DisplayName("takePhoto calls takePhoto on the imaging satellite")
    void takePhoto_ShouldCallTakePhotoOnImagingSatellite() {
        when(repository.findByConstellationName(CONSTELLATION_NAME)).thenReturn(Optional.of(constellation));
        when(constellation.getSatellites()).thenReturn(List.of(imagingSatellite));
        doNothing().when(imagingSatellite).takePhoto();

        service.takePhoto(CONSTELLATION_NAME, imagingSatellite);

        verify(imagingSatellite, times(1)).takePhoto();
        verify(repository, times(1)).save(constellation);
    }

    @Test
    @DisplayName("activateSatellite changes the managed instance, not the detached one passed in")
    void activateSatellite_ShouldActivateManagedInstance() {
        Satellite detached = mock(Satellite.class);
        when(detached.getId()).thenReturn(5L);
        when(satellite.getId()).thenReturn(5L);
        when(repository.findByConstellationName(CONSTELLATION_NAME)).thenReturn(Optional.of(constellation));
        when(constellation.getSatellites()).thenReturn(List.of(satellite));

        service.activateSatellite(CONSTELLATION_NAME, detached);

        verify(satellite).activate();
        verify(detached, never()).activate();
        verify(repository).save(constellation);
    }

    @Test
    @DisplayName("activateSatellite throws ResourceNotFoundException when the satellite is not in the constellation")
    void activateSatellite_ShouldThrow_WhenSatelliteIsNotInConstellation() {
        when(repository.findByConstellationName(CONSTELLATION_NAME)).thenReturn(Optional.of(constellation));
        when(constellation.getSatellites()).thenReturn(List.of());

        assertThrows(ResourceNotFoundException.class, () -> service.activateSatellite(CONSTELLATION_NAME, satellite));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("removeSatellite removes the satellite and publishes a deleted event")
    void removeSatellite_ShouldPublishDeletedEvent() {
        when(repository.findByConstellationName(CONSTELLATION_NAME)).thenReturn(Optional.of(constellation));
        when(constellation.getSatellites()).thenReturn(List.of(satellite));

        service.removeSatellite(CONSTELLATION_NAME, satellite);

        verify(constellation).removeSatellite(satellite);
        verify(satelliteEventPublisher).publishDeleted(satellite);
    }

    @Test
    @DisplayName("delete publishes a deleted event for every satellite and deletes the constellation")
    void delete_ShouldPublishDeletedEventForEverySatellite() {
        when(repository.findByConstellationName(CONSTELLATION_NAME)).thenReturn(Optional.of(constellation));
        when(constellation.getSatellites()).thenReturn(List.of(imagingSatellite, communicationSatellite));

        service.delete(CONSTELLATION_NAME);

        verify(satelliteEventPublisher).publishDeleted(imagingSatellite);
        verify(satelliteEventPublisher).publishDeleted(communicationSatellite);
        verify(repository).delete(constellation);
    }

    @Test
    @DisplayName("getStatus returns the status report of the constellation")
    void getStatus_ShouldReturnStatusReport() {
        String expectedStatus = "Status Report";
        when(repository.findByConstellationName(CONSTELLATION_NAME)).thenReturn(Optional.of(constellation));
        when(constellation.getStatusReport()).thenReturn(expectedStatus);

        String result = service.getStatus(CONSTELLATION_NAME);

        assertEquals(expectedStatus, result);
        verify(constellation, times(1)).getStatusReport();
    }

    @Test
    @DisplayName("getOrThrow throws ResourceNotFoundException when the constellation does not exist")
    void getOrThrow_ShouldThrowException_WhenConstellationNotFound() {
        when(repository.findByConstellationName(NON_EXISTENT_NAME)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> service.getStatus(NON_EXISTENT_NAME)
        );
        assertTrue(exception.getMessage().contains("not found"));
    }

    @Test
    @DisplayName("getSatellitesByType returns the satellites of the given type")
    void getSatellitesByType_ShouldReturnSatellitesOfSpecificType() {
        List<ImagingSatellite> expectedSatellites = Arrays.asList(imagingSatellite);
        when(repository.findByConstellationName(CONSTELLATION_NAME)).thenReturn(Optional.of(constellation));
        when(constellation.getSatellitesByType(ImagingSatellite.class)).thenReturn(expectedSatellites);

        List<ImagingSatellite> result = service.getSatellitesByType(CONSTELLATION_NAME, ImagingSatellite.class);

        assertEquals(expectedSatellites, result);
        verify(constellation, times(1)).getSatellitesByType(ImagingSatellite.class);
    }
}
