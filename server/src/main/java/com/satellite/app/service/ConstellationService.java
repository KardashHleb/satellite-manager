package com.satellite.app.service;

import com.satellite.app.exception.ResourceNotFoundException;
import com.satellite.app.model.CommunicationSatellite;
import com.satellite.app.model.ImagingSatellite;
import com.satellite.app.model.Satellite;
import com.satellite.app.model.SatelliteConstellation;
import com.satellite.app.kafka.SatelliteEventPublisher;
import com.satellite.app.repository.ConstellationRepository;
import com.satellite.app.repository.SatelliteRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final SatelliteCacheEvictionService cacheEvictionService;

    @Autowired
    public ConstellationService(ConstellationRepository repository,
                                SatelliteRepository satelliteRepository,
                                SatelliteEventPublisher satelliteEventPublisher,
                                SatelliteCacheEvictionService cacheEvictionService) {
        this.repository = repository;
        this.satelliteRepository = satelliteRepository;
        this.satelliteEventPublisher = satelliteEventPublisher;
        this.cacheEvictionService = cacheEvictionService;
    }

    public SatelliteConstellation create(String name) {
        if (repository.existsByConstellationName(name)) {
            throw new IllegalArgumentException("Constellation already exists: " + name);
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
        SatelliteConstellation constellation = getOrThrow(name);
        List<Satellite> satellites = constellation.getSatellites();
        satellites.forEach(satelliteEventPublisher::publishDeleted);
        repository.delete(constellation);
        cacheEvictionService.constellationChanged(name, satelliteIds(satellites));
    }

    public void addSatellite(String constellationName, Satellite satellite) {
        SatelliteConstellation constellation = getOrThrow(constellationName);
        constellation.addSatellite(satellite);
        repository.saveAndFlush(constellation);
        Satellite persisted = satelliteRepository
                .findByConstellation_ConstellationNameAndName(constellationName, satellite.getName())
                .orElse(satellite);
        satelliteEventPublisher.publishCreated(persisted);
        cacheEvictionService.satelliteChanged(persisted.getId(), constellationName);
    }

    public void removeSatellite(String constellationName, Satellite satellite) {
        SatelliteConstellation constellation = getOrThrow(constellationName);
        Satellite managed = managedSatellite(constellation, satellite);
        constellation.removeSatellite(managed);
        satelliteEventPublisher.publishDeleted(managed);
        repository.save(constellation);
        cacheEvictionService.satelliteChanged(managed.getId(), constellationName);
    }

    public void executeMissions(String constellationName) {
        SatelliteConstellation constellation = getOrThrow(constellationName);
        constellation.executeAllMissions();
        repository.save(constellation);
        cacheEvictionService.constellationChanged(constellationName, satelliteIds(constellation.getSatellites()));
    }

    public void activateSatellite(String constellationName, Satellite satellite) {
        SatelliteConstellation constellation = getOrThrow(constellationName);
        Satellite managed = managedSatellite(constellation, satellite);
        managed.activate();
        repository.save(constellation);
        cacheEvictionService.satelliteChanged(managed.getId(), constellationName);
    }

    public void deactivateSatellite(String constellationName, Satellite satellite) {
        SatelliteConstellation constellation = getOrThrow(constellationName);
        Satellite managed = managedSatellite(constellation, satellite);
        managed.deactivate();
        repository.save(constellation);
        cacheEvictionService.satelliteChanged(managed.getId(), constellationName);
    }

    public void takePhoto(String constellationName, ImagingSatellite satellite) {
        SatelliteConstellation constellation = getOrThrow(constellationName);
        ImagingSatellite managed = managedSatellite(constellation, satellite);
        managed.takePhoto();
        repository.save(constellation);
        cacheEvictionService.satelliteChanged(managed.getId(), constellationName);
    }

    public void sendData(String constellationName, CommunicationSatellite satellite, double dataSize) {
        SatelliteConstellation constellation = getOrThrow(constellationName);
        CommunicationSatellite managed = managedSatellite(constellation, satellite);
        managed.sendData(dataSize);
        repository.save(constellation);
        cacheEvictionService.satelliteChanged(managed.getId(), constellationName);
    }

    public void performSatelliteMission(String constellationName, Satellite satellite) {
        SatelliteConstellation constellation = getOrThrow(constellationName);
        Satellite managed = managedSatellite(constellation, satellite);
        managed.performMission();
        repository.save(constellation);
        cacheEvictionService.satelliteChanged(managed.getId(), constellationName);
    }

    public void activateAll(String constellationName) {
        SatelliteConstellation constellation = getOrThrow(constellationName);
        constellation.activateAllSatellites();
        repository.save(constellation);
        cacheEvictionService.constellationChanged(constellationName, satelliteIds(constellation.getSatellites()));
    }

    public void deactivateAll(String constellationName) {
        SatelliteConstellation constellation = getOrThrow(constellationName);
        constellation.deactivateAllSatellites();
        repository.save(constellation);
        cacheEvictionService.constellationChanged(constellationName, satelliteIds(constellation.getSatellites()));
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
                .orElseThrow(() -> new ResourceNotFoundException("Constellation not found: " + name));
    }

    /**
     * Callers pass satellites that were loaded in an earlier transaction (detached entities).
     * Changes made to such an instance are not saved, so the operation is applied to the managed
     * instance with the same id from the constellation loaded in the current transaction.
     */
    @SuppressWarnings("unchecked")
    private <T extends Satellite> T managedSatellite(SatelliteConstellation constellation, T satellite) {
        for (Satellite candidate : constellation.getSatellites()) {
            if (candidate == satellite
                    || (satellite.getId() != null && satellite.getId().equals(candidate.getId()))) {
                return (T) candidate;
            }
        }
        throw new ResourceNotFoundException("Satellite not found: " + satellite.getName()
                + " in constellation: " + constellation.getConstellationName());
    }

    private static List<Long> satelliteIds(List<Satellite> satellites) {
        return satellites.stream().map(Satellite::getId).toList();
    }
}
