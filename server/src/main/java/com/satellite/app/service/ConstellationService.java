package com.satellite.app.service;

import com.satellite.app.model.CommunicationSatellite;
import com.satellite.app.model.ImagingSatellite;
import com.satellite.app.model.Satellite;
import com.satellite.app.model.SatelliteConstellation;
import com.satellite.app.kafka.SatelliteEventPublisher;
import com.satellite.app.repository.ConstellationRepository;
import com.satellite.app.repository.SatelliteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class ConstellationService {

    private final ConstellationRepository repository;
    private final SatelliteRepository satelliteRepository;
    private final SatelliteEventPublisher satelliteEventPublisher;

    @Autowired
    public ConstellationService(ConstellationRepository repository,
                                SatelliteRepository satelliteRepository,
                                SatelliteEventPublisher satelliteEventPublisher) {
        this.repository = repository;
        this.satelliteRepository = satelliteRepository;
        this.satelliteEventPublisher = satelliteEventPublisher;
    }

    public SatelliteConstellation create(String name) {
        if (repository.existsByConstellationName(name)) {
            throw new IllegalArgumentException("Группировка уже существует: " + name);
        }
        return repository.save(new SatelliteConstellation(name));
    }

    @Transactional(readOnly = true)
    public Optional<SatelliteConstellation> get(String name) {
        return repository.findByConstellationName(name);
    }

    @Transactional(readOnly = true)
    public Map<String, SatelliteConstellation> getAll() {
        return repository.findAllWithSatellitesFetch().stream()
                .collect(Collectors.toMap(
                        SatelliteConstellation::getConstellationName,
                        Function.identity(),
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }

    public void delete(String name) {
        repository.deleteByConstellationName(name);
    }

    @Caching(evict = {
            @CacheEvict(value = "constellation", key = "#constellationName"),
            @CacheEvict(value = "satellites", allEntries = true)
    })
    public void addSatellite(String constellationName, Satellite satellite) {
        SatelliteConstellation constellation = getOrThrow(constellationName);
        constellation.addSatellite(satellite);
        repository.saveAndFlush(constellation);
        Satellite persisted = satelliteRepository
                .findByConstellation_ConstellationNameAndName(constellationName, satellite.getName())
                .orElse(satellite);
        satelliteEventPublisher.publishCreated(persisted);
    }

    public void removeSatellite(String constellationName, Satellite satellite) {
        SatelliteConstellation constellation = getOrThrow(constellationName);
        constellation.removeSatellite(satellite);
        repository.save(constellation);
    }

    public void executeMissions(String constellationName) {
        SatelliteConstellation constellation = getOrThrow(constellationName);
        constellation.executeAllMissions();
        repository.save(constellation);
    }

    public void activateSatellite(String constellationName, Satellite satellite) {
        SatelliteConstellation constellation = getOrThrow(constellationName);
        satellite.activate();
        repository.save(constellation);
    }

    public void deactivateSatellite(String constellationName, Satellite satellite) {
        SatelliteConstellation constellation = getOrThrow(constellationName);
        satellite.deactivate();
        repository.save(constellation);
    }

    public void takePhoto(String constellationName, ImagingSatellite satellite) {
        SatelliteConstellation constellation = getOrThrow(constellationName);
        satellite.takePhoto();
        repository.save(constellation);
    }

    public void sendData(String constellationName, CommunicationSatellite satellite, double dataSize) {
        SatelliteConstellation constellation = getOrThrow(constellationName);
        satellite.sendData(dataSize);
        repository.save(constellation);
    }

    public void performSatelliteMission(String constellationName, Satellite satellite) {
        SatelliteConstellation constellation = getOrThrow(constellationName);
        satellite.performMission();
        repository.save(constellation);
    }

    public void activateAll(String constellationName) {
        SatelliteConstellation constellation = getOrThrow(constellationName);
        constellation.activateAllSatellites();
        repository.save(constellation);
    }

    public void deactivateAll(String constellationName) {
        SatelliteConstellation constellation = getOrThrow(constellationName);
        constellation.deactivateAllSatellites();
        repository.save(constellation);
    }

    @Transactional(readOnly = true)
    public String getStatus(String constellationName) {
        return getOrThrow(constellationName).getStatusReport();
    }

    @Transactional(readOnly = true)
    public List<Satellite> getSatellites(String constellationName) {
        return getOrThrow(constellationName).getSatellites();
    }

    @Transactional(readOnly = true)
    public <T extends Satellite> List<T> getSatellitesByType(String constellationName, Class<T> type) {
        return getOrThrow(constellationName).getSatellitesByType(type);
    }

    @Transactional(readOnly = true)
    public String getConstellationName(String constellationName) {
        return getOrThrow(constellationName).getConstellationName();
    }

    private SatelliteConstellation getOrThrow(String name) {
        return repository.findByConstellationName(name)
                .orElseThrow(() -> new IllegalArgumentException("Группировка не найдена: " + name));
    }
}
