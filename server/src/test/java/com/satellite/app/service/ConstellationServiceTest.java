package com.satellite.app.service;

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
@DisplayName("Юнит-тесты для ConstellationService")
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
    @DisplayName("create должен создать новую группировку при уникальном имени")
    void create_ShouldCreateNewConstellation_WhenNameIsUnique() {
        when(repository.existsByConstellationName(CONSTELLATION_NAME)).thenReturn(false);
        when(repository.save(any(SatelliteConstellation.class))).thenReturn(constellation);

        SatelliteConstellation result = service.create(CONSTELLATION_NAME);

        assertNotNull(result);
        verify(repository, times(1)).existsByConstellationName(CONSTELLATION_NAME);
        verify(repository, times(1)).save(any(SatelliteConstellation.class));
    }

    @Test
    @DisplayName("create должен выбросить исключение при существующем имени")
    void create_ShouldThrowException_WhenNameAlreadyExists() {
        when(repository.existsByConstellationName(CONSTELLATION_NAME)).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.create(CONSTELLATION_NAME)
        );
        assertTrue(exception.getMessage().contains("уже существует"));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("get должен вернуть группировку при существующем имени")
    void get_ShouldReturnConstellation_WhenNameExists() {
        when(repository.findByConstellationName(CONSTELLATION_NAME)).thenReturn(Optional.of(constellation));

        Optional<SatelliteConstellation> result = service.get(CONSTELLATION_NAME);

        assertTrue(result.isPresent());
        assertEquals(constellation, result.get());
        verify(repository, times(1)).findByConstellationName(CONSTELLATION_NAME);
    }

    @Test
    @DisplayName("addSatellite должен добавить спутник в группировку")
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
    @DisplayName("executeMissions должен выполнить миссии всех спутников в группировке")
    void executeMissions_ShouldExecuteAllMissionsInConstellation() {
        when(repository.findByConstellationName(CONSTELLATION_NAME)).thenReturn(Optional.of(constellation));
        doNothing().when(constellation).executeAllMissions();

        service.executeMissions(CONSTELLATION_NAME);

        verify(constellation, times(1)).executeAllMissions();
        verify(repository, times(1)).save(constellation);
    }

    @Test
    @DisplayName("takePhoto должен вызвать метод съемки у спутника")
    void takePhoto_ShouldCallTakePhotoOnImagingSatellite() {
        when(repository.findByConstellationName(CONSTELLATION_NAME)).thenReturn(Optional.of(constellation));
        doNothing().when(imagingSatellite).takePhoto();

        service.takePhoto(CONSTELLATION_NAME, imagingSatellite);

        verify(imagingSatellite, times(1)).takePhoto();
        verify(repository, times(1)).save(constellation);
    }

    @Test
    @DisplayName("getStatus должен вернуть отчет о статусе группировки")
    void getStatus_ShouldReturnStatusReport() {
        String expectedStatus = "Status Report";
        when(repository.findByConstellationName(CONSTELLATION_NAME)).thenReturn(Optional.of(constellation));
        when(constellation.getStatusReport()).thenReturn(expectedStatus);

        String result = service.getStatus(CONSTELLATION_NAME);

        assertEquals(expectedStatus, result);
        verify(constellation, times(1)).getStatusReport();
    }

    @Test
    @DisplayName("getOrThrow должен выбросить исключение при отсутствии группировки")
    void getOrThrow_ShouldThrowException_WhenConstellationNotFound() {
        when(repository.findByConstellationName(NON_EXISTENT_NAME)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.getStatus(NON_EXISTENT_NAME)
        );
        assertTrue(exception.getMessage().contains("не найдена"));
    }

    @Test
    @DisplayName("getSatellitesByType должен вернуть спутники определенного типа")
    void getSatellitesByType_ShouldReturnSatellitesOfSpecificType() {
        List<ImagingSatellite> expectedSatellites = Arrays.asList(imagingSatellite);
        when(repository.findByConstellationName(CONSTELLATION_NAME)).thenReturn(Optional.of(constellation));
        when(constellation.getSatellitesByType(ImagingSatellite.class)).thenReturn(expectedSatellites);

        List<ImagingSatellite> result = service.getSatellitesByType(CONSTELLATION_NAME, ImagingSatellite.class);

        assertEquals(expectedSatellites, result);
        verify(constellation, times(1)).getSatellitesByType(ImagingSatellite.class);
    }
}
