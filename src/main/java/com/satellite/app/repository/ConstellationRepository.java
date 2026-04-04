package com.satellite.app.repository;

import com.satellite.app.model.SatelliteConstellation;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
public class ConstellationRepository {

    // Хранилище группировок: ключ - имя, значение - объект группировки
    private final Map<String, SatelliteConstellation> constellations = new HashMap<>();

    // CRUD операции

    // Create - сохранение новой группировки
    public SatelliteConstellation save(SatelliteConstellation constellation) {
        constellations.put(constellation.getConstellationName(), constellation);
        return constellation;
    }

    // Read - получение группировки по имени
    public Optional<SatelliteConstellation> findByName(String name) {
        return Optional.ofNullable(constellations.get(name));
    }

    // Read - получение всех группировок
    public Map<String, SatelliteConstellation> findAll() {
        return new HashMap<>(constellations); // Возвращаем копию для безопасности
    }

    // Update - обновление существующей группировки
    public SatelliteConstellation update(SatelliteConstellation constellation) {
        if (constellations.containsKey(constellation.getConstellationName())) {
            constellations.put(constellation.getConstellationName(), constellation);
            return constellation;
        }
        throw new IllegalArgumentException("Constellation not found: " + constellation.getConstellationName());
    }

    // Delete - удаление группировки
    public void deleteByName(String name) {
        constellations.remove(name);
    }

    // Проверка существования группировки
    public boolean existsByName(String name) {
        return constellations.containsKey(name);
    }

    // Получение количества группировок
    public int count() {
        return constellations.size();
    }

    // Очистка хранилища (для тестов)
    public void clear() {
        constellations.clear();
    }
}