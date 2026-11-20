package com.satellite.app.service;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

@Service
public class SatelliteCacheEvictionService {

    @CacheEvict(value = "satellite", key = "#satelliteId")
    public void evictSatellite(Long satelliteId) {
    }

    @CacheEvict(value = "satellites", allEntries = true)
    public void evictAllSatellites() {
    }

    @CacheEvict(value = "constellation", key = "#constellationName")
    public void evictConstellation(String constellationName) {
    }
}
