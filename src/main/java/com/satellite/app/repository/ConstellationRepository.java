package com.satellite.app.repository;

import com.satellite.app.model.SatelliteConstellation;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
public class ConstellationRepository {

    // Constellation storage: key - name, value - constellation object
    private final Map<String, SatelliteConstellation> constellations = new HashMap<>();

    // CRUD operations

    // Create - save a new constellation
    public SatelliteConstellation save(SatelliteConstellation constellation) {
        constellations.put(constellation.getConstellationName(), constellation);
        return constellation;
    }

    // Read - get a constellation by name
    public Optional<SatelliteConstellation> findByName(String name) {
        return Optional.ofNullable(constellations.get(name));
    }

    // Read - get all constellations
    public Map<String, SatelliteConstellation> findAll() {
        return new HashMap<>(constellations); // Return a copy for safety
    }

    // Update - update an existing constellation
    public SatelliteConstellation update(SatelliteConstellation constellation) {
        if (constellations.containsKey(constellation.getConstellationName())) {
            constellations.put(constellation.getConstellationName(), constellation);
            return constellation;
        }
        throw new IllegalArgumentException("Constellation not found: " + constellation.getConstellationName());
    }

    // Delete - remove a constellation
    public void deleteByName(String name) {
        constellations.remove(name);
    }

    // Check whether a constellation exists
    public boolean existsByName(String name) {
        return constellations.containsKey(name);
    }

    // Get the number of constellations
    public int count() {
        return constellations.size();
    }

    // Clear the storage (for tests)
    public void clear() {
        constellations.clear();
    }
}