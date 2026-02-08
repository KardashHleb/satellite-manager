package com.satellite.app.service;

import com.satellite.app.CommunicationSatellite;
import com.satellite.app.ImagingSatellite;
import com.satellite.app.Satellite;
import com.satellite.app.SatelliteConstellation;
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
@DisplayName("Юнит-тесты для SpaceOperationCenterService")
class SpaceOperationCenterServiceTest {

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
    private SpaceOperationCenterService service;

    @BeforeEach
    void setUp() {
        // Общая настройка для моков
        lenient().when(constellation.getConstellationName()).thenReturn(CONSTELLATION_NAME);
    }

    @Test
    @DisplayName("create должен создать новую группировку при уникальном имени")
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
    @DisplayName("create должен выбросить исключение при существующем имени")
    void create_ShouldThrowException_WhenNameAlreadyExists() {
        // Arrange
        when(repository.existsByName(CONSTELLATION_NAME)).thenReturn(true);

        // Act & Assert
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
    @DisplayName("addSatellite должен добавить спутник в группировку")
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
    @DisplayName("executeMissions должен выполнить миссии всех спутников в группировке")
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
    @DisplayName("takePhoto должен вызвать метод съемки у спутника")
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
    @DisplayName("getStatus должен вернуть отчет о статусе группировки")
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
    @DisplayName("getOrThrow должен выбросить исключение при отсутствии группировки")
    void getOrThrow_ShouldThrowException_WhenConstellationNotFound() {
        // Arrange
        when(repository.findByName(NON_EXISTENT_NAME)).thenReturn(Optional.empty());

        // Act & Assert (используем рефлексию для тестирования приватного метода)
        // В реальном проекте можно использовать Spring Test или изменить модификатор доступа
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.getStatus(NON_EXISTENT_NAME)
        );
        assertTrue(exception.getMessage().contains("не найдена"));
    }

    @Test
    @DisplayName("getSatellitesByType должен вернуть спутники определенного типа")
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