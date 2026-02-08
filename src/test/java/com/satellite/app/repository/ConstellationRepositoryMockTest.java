package com.satellite.app.repository;

import com.satellite.app.SatelliteConstellation;
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
@DisplayName("Mock тесты для ConstellationRepository")
class ConstellationRepositoryMockTest {

    @Mock
    private SatelliteConstellation mockConstellation;

    @InjectMocks
    private ConstellationRepository repository;

    private Map<String, SatelliteConstellation> testMap;

    @BeforeEach
    void setUp() {
        // Используем Reflection для подмены внутреннего Map
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
    @DisplayName("Тест 1: Сохранение новой группировки успешно добавляет ее в хранилище")
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
    @DisplayName("Тест 2: Поиск существующей группировки возвращает Optional с объектом")
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
    @DisplayName("Тест 3: Поиск несуществующей группировки возвращает пустой Optional")
    void findNonExistingConstellationReturnsEmpty() {
        // Act
        Optional<SatelliteConstellation> result = repository.findByName("NonExisting");

        // Assert
        assertFalse(result.isPresent());
        assertEquals(0, testMap.size());
    }

    @Test
    @DisplayName("Тест 4: Удаление существующей группировки удаляет ее из хранилища")
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
    @DisplayName("Тест 5: Проверка существования группировки возвращает корректный результат")
    void existsByNameReturnsCorrectResult() {
        // Arrange
        when(mockConstellation.getConstellationName()).thenReturn("Existing");
        repository.save(mockConstellation);

        // Act & Assert для существующей
        assertTrue(repository.existsByName("Existing"));

        // Act & Assert для несуществующей
        assertFalse(repository.existsByName("NonExisting"));
    }
}