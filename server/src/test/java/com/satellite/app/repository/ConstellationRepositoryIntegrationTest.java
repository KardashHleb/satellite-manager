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
@DisplayName("Интеграционный тест ConstellationRepository с моками")
class ConstellationRepositoryIntegrationTest {

    @InjectMocks
    private ConstellationRepository repository;

    // Мок для внутреннего хранилища (чтобы протестировать бизнес-логику)
    private Map<String, SatelliteConstellation> mockConstellations;

    private static final String TEST_CONSTELLATION_NAME = "IntegrationTestGroup";
    private static final String COMMUNICATION_SATELLITE_NAME = "ComSat-Integration";
    private static final String IMAGING_SATELLITE_NAME = "ImgSat-Integration";

    private CommunicationSatellite communicationSatellite;
    private ImagingSatellite imagingSatellite;

    @BeforeEach
    void setUp() {
        // Создаем реальные объекты для тестирования
        communicationSatellite = new CommunicationSatellite(COMMUNICATION_SATELLITE_NAME, 0.85, 500.0);
        imagingSatellite = new ImagingSatellite(IMAGING_SATELLITE_NAME, 0.75, 2.5);

        // Инициализируем mock-хранилище
        mockConstellations = new HashMap<>();

        // Используем Reflection для подмены внутреннего хранилища
        try {
            var field = ConstellationRepository.class.getDeclaredField("constellations");
            field.setAccessible(true);
            field.set(repository, mockConstellations);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set up test", e);
        }
    }

    @Test
    @DisplayName("Тест 1: Полный жизненный цикл группировки спутников через репозиторий")
    void fullConstellationLifecycleTest() {
        // 1. Создание группировки
        SatelliteConstellation constellation = new SatelliteConstellation(TEST_CONSTELLATION_NAME);
        assertNotNull(constellation, "Группировка должна создаваться успешно");
        assertEquals(TEST_CONSTELLATION_NAME, constellation.getConstellationName(),
                "Имя группировки должно соответствовать заданному");

        // 2. Добавление спутников разных типов
        constellation.addSatellite(communicationSatellite);
        constellation.addSatellite(imagingSatellite);
        assertEquals(2, constellation.getSatellites().size(),
                "В группировке должно быть 2 спутника");

        // 3. Сохранение в репозиторий
        SatelliteConstellation savedConstellation = repository.save(constellation);
        assertNotNull(savedConstellation, "Сохраненная группировка не должна быть null");
        assertTrue(repository.existsByName(TEST_CONSTELLATION_NAME),
                "Группировка должна существовать после сохранения");

        // 4. Извлечение из репозитория
        Optional<SatelliteConstellation> retrievedOpt = repository.findByName(TEST_CONSTELLATION_NAME);
        assertTrue(retrievedOpt.isPresent(), "Группировка должна быть найдена по имени");

        SatelliteConstellation retrievedConstellation = retrievedOpt.get();
        assertEquals(2, retrievedConstellation.getSatellites().size(),
                "Извлеченная группировка должна содержать 2 спутника");

        // 5. Изменение состояния спутников
        retrievedConstellation.activateAllSatellites();
        assertTrue(retrievedConstellation.getSatellites().get(0).isActive(),
                "Первый спутник должен быть активен после активации");
        assertTrue(retrievedConstellation.getSatellites().get(1).isActive(),
                "Второй спутник должен быть активен после активации");

        // 6. Обновление в репозитории
        SatelliteConstellation updatedConstellation = repository.update(retrievedConstellation);
        assertEquals(2, updatedConstellation.getActiveSatelliteCount(),
                "После обновления должно быть 2 активных спутника");

        // 7. Выполнение миссий и проверка изменений
        double initialBattery1 = updatedConstellation.getSatellites().get(0).getBatteryLevel();
        double initialBattery2 = updatedConstellation.getSatellites().get(1).getBatteryLevel();

        updatedConstellation.executeAllMissions();

        double finalBattery1 = updatedConstellation.getSatellites().get(0).getBatteryLevel();
        double finalBattery2 = updatedConstellation.getSatellites().get(1).getBatteryLevel();

        assertTrue(finalBattery1 < initialBattery1,
                "Заряд батареи первого спутника должен уменьшиться после выполнения миссии");
        assertTrue(finalBattery2 < initialBattery2,
                "Заряд батареи второго спутника должен уменьшиться после выполнения миссии");

        // 8. Финальное обновление и проверка сохранности данных
        SatelliteConstellation finalUpdated = repository.update(updatedConstellation);
        Optional<SatelliteConstellation> finalRetrieved = repository.findByName(TEST_CONSTELLATION_NAME);

        assertTrue(finalRetrieved.isPresent(), "Группировка должна сохраняться до конца теста");
        assertEquals(2, finalRetrieved.get().getSatellites().size(),
                "Финальное состояние должно содержать 2 спутника");

        // 9. Проверка счетчика группировок
        assertEquals(1, repository.count(), "Должна быть одна группировка в репозитории");
    }

    @Test
    @DisplayName("Тест 2: Работа с несколькими группировками и проверка изолированности данных")
    void multipleConstellationsIsolationTest() {
        // Создаем первую группировку
        SatelliteConstellation group1 = new SatelliteConstellation("GroupAlpha");
        group1.addSatellite(new CommunicationSatellite("AlphaSat1", 0.9, 600.0));
        repository.save(group1);

        // Создаем вторую группировку
        SatelliteConstellation group2 = new SatelliteConstellation("GroupBeta");
        group2.addSatellite(new ImagingSatellite("BetaSat1", 0.8, 1.5));
        group2.addSatellite(new ImagingSatellite("BetaSat2", 0.7, 2.0));
        repository.save(group2);

        // Проверяем сохранение обеих группировок
        assertTrue(repository.existsByName("GroupAlpha"), "Первая группировка должна существовать");
        assertTrue(repository.existsByName("GroupBeta"), "Вторая группировка должна существовать");

        // Получаем все группировки
        Map<String, SatelliteConstellation> allGroups = repository.findAll();
        assertEquals(2, allGroups.size(), "Должно быть 2 группировки в репозитории");

        // Проверяем изолированность данных
        SatelliteConstellation retrievedGroup1 = repository.findByName("GroupAlpha")
                .orElseThrow(() -> new AssertionError("GroupAlpha не найден"));
        SatelliteConstellation retrievedGroup2 = repository.findByName("GroupBeta")
                .orElseThrow(() -> new AssertionError("GroupBeta не найден"));

        // Группировки должны быть независимыми
        assertEquals(1, retrievedGroup1.getSatellites().size(),
                "GroupAlpha должна содержать 1 спутник");
        assertEquals(2, retrievedGroup2.getSatellites().size(),
                "GroupBeta должна содержать 2 спутника");

        // Изменяем одну группировку
        retrievedGroup1.activateAllSatellites();
        repository.update(retrievedGroup1);

        // Проверяем, что другая группировка не изменилась
        SatelliteConstellation group2AfterUpdate = repository.findByName("GroupBeta")
                .orElseThrow(() -> new AssertionError("GroupBeta не найден после обновления"));

        assertFalse(group2AfterUpdate.getSatellites().get(0).isActive(),
                "Спутники GroupBeta не должны активироваться при изменении GroupAlpha");

        // Удаляем одну группировку
        repository.deleteByName("GroupAlpha");

        // Проверяем, что другая осталась
        assertFalse(repository.existsByName("GroupAlpha"),
                "GroupAlpha должна быть удалена");
        assertTrue(repository.existsByName("GroupBeta"),
                "GroupBeta должна остаться после удаления GroupAlpha");

        // Проверяем счетчик
        assertEquals(1, repository.count(),
                "После удаления одной группировки должна остаться одна");
    }

    @Test
    @DisplayName("Тест 3: Обновление несуществующей группировки выбрасывает исключение")
    void updateNonExistentConstellationThrowsException() {
        // Arrange
        SatelliteConstellation nonExistentConstellation = new SatelliteConstellation("NonExistent");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                repository.update(nonExistentConstellation)
        );
    }

    @Test
    @DisplayName("Тест 4: Очистка репозитория")
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
    @DisplayName("Тест 5: Получение всех группировок возвращает копию")
    void findAllReturnsCopyTest() {
        // Arrange
        SatelliteConstellation constellation = new SatelliteConstellation("TestGroup");
        repository.save(constellation);

        // Act
        Map<String, SatelliteConstellation> allGroups = repository.findAll();
        allGroups.put("NewKey", new SatelliteConstellation("NewGroup"));

        // Assert - оригинальный репозиторий не должен измениться
        assertEquals(1, repository.count());
        assertFalse(repository.existsByName("NewGroup"));
    }
}