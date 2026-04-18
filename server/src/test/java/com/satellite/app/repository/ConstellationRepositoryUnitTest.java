package com.satellite.app.repository;

import com.satellite.app.model.SatelliteConstellation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unit тесты для ConstellationRepository")
class ConstellationRepositoryUnitTest {

    // Константы для тестовых данных
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
        // Используем конструктор с одним параметром (только имя)
        existingConstellation = new SatelliteConstellation(EXISTING_CONSTELLATION_NAME);
        repository.save(existingConstellation);
        repository.save(new SatelliteConstellation(ANOTHER_EXISTING_NAME));
    }

    @Test
    @DisplayName("Сохранение новой группировки должно добавлять ее в хранилище")
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
    @DisplayName("Сохранение с существующим именем должно перезаписывать группировку")
    void save_ExistingName_ShouldOverwrite() {
        // Arrange
        SatelliteConstellation updated = new SatelliteConstellation(EXISTING_CONSTELLATION_NAME);

        // Act
        SatelliteConstellation result = repository.save(updated);

        // Assert
        assertEquals(EXISTING_CONSTELLATION_NAME, result.getConstellationName());
        assertEquals(2, repository.count()); // Количество не должно измениться

        // Проверяем, что объект был заменен
        Optional<SatelliteConstellation> found = repository.findByName(EXISTING_CONSTELLATION_NAME);
        assertTrue(found.isPresent());
        // Это новый объект, но с тем же именем
        assertEquals(EXISTING_CONSTELLATION_NAME, found.get().getConstellationName());
    }

    @Test
    @DisplayName("Поиск существующей группировки должен вернуть Optional с объектом")
    void findByName_ExistingConstellation_ShouldReturnOptionalWithValue() {
        // Act
        Optional<SatelliteConstellation> result = repository.findByName(EXISTING_CONSTELLATION_NAME);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(existingConstellation, result.get());
        assertEquals(EXISTING_CONSTELLATION_NAME, result.get().getConstellationName());
    }

    @Test
    @DisplayName("Поиск несуществующей группировки должен вернуть пустой Optional")
    void findByName_NonExistingConstellation_ShouldReturnEmptyOptional() {
        // Act & Assert
        assertFalse(repository.findByName(NON_EXISTING_NAME).isPresent());
    }

    @Test
    @DisplayName("Получение всех группировок должно вернуть защитную копию")
    void findAll_ShouldReturnDefensiveCopy() {
        // Act
        Map<String, SatelliteConstellation> allConstellations = repository.findAll();

        // Модифицируем возвращенную карту
        allConstellations.put("Tampered", new SatelliteConstellation("Tampered"));

        // Assert - оригинальное хранилище не должно измениться
        assertFalse(repository.existsByName("Tampered"));
        assertEquals(2, repository.count());
    }

    @Test
    @DisplayName("Обновление существующей группировки должно изменять данные")
    void update_ExistingConstellation_ShouldUpdateData() {
        // Arrange
        SatelliteConstellation updated = new SatelliteConstellation(EXISTING_CONSTELLATION_NAME);

        // Act
        SatelliteConstellation result = repository.update(updated);

        // Assert
        assertEquals(EXISTING_CONSTELLATION_NAME, result.getConstellationName());

        // Проверяем, что объект был обновлен
        Optional<SatelliteConstellation> found = repository.findByName(EXISTING_CONSTELLATION_NAME);
        assertTrue(found.isPresent());
        // Это новый объект после обновления
        assertEquals(EXISTING_CONSTELLATION_NAME, found.get().getConstellationName());
    }

    @Test
    @DisplayName("Обновление несуществующей группировки должно бросать исключение")
    void update_NonExistingConstellation_ShouldThrowException() {
        // Arrange
        SatelliteConstellation nonExisting = new SatelliteConstellation(NON_EXISTING_NAME);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> repository.update(nonExisting));
    }

    @Test
    @DisplayName("Удаление существующей группировки должно удалять ее из хранилища")
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
    @DisplayName("Удаление несуществующей группировки не должно влиять на хранилище")
    void deleteByName_NonExistingConstellation_ShouldDoNothing() {
        // Arrange
        int initialCount = repository.count();

        // Act
        repository.deleteByName(NON_EXISTING_NAME);

        // Assert
        assertEquals(initialCount, repository.count());
    }

    @Test
    @DisplayName("Проверка существования должна корректно определять наличие группировки")
    void existsByName_ShouldCorrectlyIdentifyPresence() {
        // Act & Assert
        assertTrue(repository.existsByName(EXISTING_CONSTELLATION_NAME));
        assertFalse(repository.existsByName(NON_EXISTING_NAME));
    }

    @Test
    @DisplayName("Подсчет должен возвращать корректное количество группировок")
    void count_ShouldReturnCorrectNumber() {
        // Act & Assert
        assertEquals(2, repository.count());

        // Добавляем еще одну
        repository.save(new SatelliteConstellation("Third"));
        assertEquals(3, repository.count());
    }

    @Test
    @DisplayName("Сохранение null должно обрабатываться корректно")
    void save_NullConstellation_ShouldThrowException() {
        assertThrows(NullPointerException.class, () -> repository.save(null));
    }

    @Test
    @DisplayName("Очистка должна удалять все группировки из хранилища")
    void clear_ShouldRemoveAllConstellations() {
        // Act
        repository.clear();

        // Assert
        assertEquals(0, repository.count());
        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    @DisplayName("Работа с пустым именем должна корректно обрабатываться")
    void operations_WithEmptyName_ShouldWorkCorrectly() {
        // Arrange
        SatelliteConstellation emptyName = new SatelliteConstellation(EMPTY_NAME);

        // Act & Assert для save
        repository.save(emptyName);
        assertTrue(repository.existsByName(EMPTY_NAME));

        // Act & Assert для findByName
        assertTrue(repository.findByName(EMPTY_NAME).isPresent());

        // Act & Assert для deleteByName
        repository.deleteByName(EMPTY_NAME);
        assertFalse(repository.existsByName(EMPTY_NAME));
    }
}