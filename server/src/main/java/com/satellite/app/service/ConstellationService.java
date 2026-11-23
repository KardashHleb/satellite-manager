package com.satellite.app.service;

import com.satellite.app.exception.NotFoundException;
import com.satellite.app.model.CommunicationSatellite;
import com.satellite.app.model.ImagingSatellite;
import com.satellite.app.model.Satellite;
import com.satellite.app.model.SatelliteConstellation;
import com.satellite.app.kafka.SatelliteEventPublisher;
import com.satellite.app.repository.ConstellationRepository;
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
    private final SatelliteEventPublisher satelliteEventPublisher;

    @Autowired
    public ConstellationService(ConstellationRepository repository,
                                SatelliteEventPublisher satelliteEventPublisher) {
        this.repository = repository;
        this.satelliteEventPublisher = satelliteEventPublisher;
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
        repository.deleteByConstellationName(name);
    }

    public void addSatellite(String constellationName, Satellite satellite) {
        SatelliteConstellation constellation = getOrThrow(constellationName);
        constellation.addSatellite(satellite);
        repository.save(constellation);
        // save() merges the new satellite into a managed copy with an id; the event must carry that copy
        Satellite persisted = constellation.getSatellites().stream()
                .filter(s -> s.getId() != null && s.getName().equals(satellite.getName()))
                .findFirst()
                .orElse(satellite);
        satelliteEventPublisher.publishCreated(persisted);
    }

    public void removeSatellite(String constellationName, Satellite satellite) {
        SatelliteConstellation constellation = getOrThrow(constellationName);
        constellation.removeSatellite(attached(constellation, satellite));
        repository.save(constellation);
    }

    public void executeMissions(String constellationName) {
        SatelliteConstellation constellation = getOrThrow(constellationName);
        constellation.executeAllMissions();
        repository.save(constellation);
    }

    // Returns true if the satellite was turned on by this call
    public boolean activateSatellite(String constellationName, Satellite satellite) {
        SatelliteConstellation constellation = getOrThrow(constellationName);
        boolean activated = attached(constellation, satellite).activate();
        repository.save(constellation);
        return activated;
    }

    public void deactivateSatellite(String constellationName, Satellite satellite) {
        SatelliteConstellation constellation = getOrThrow(constellationName);
        attached(constellation, satellite).deactivate();
        repository.save(constellation);
    }

    public void takePhoto(String constellationName, ImagingSatellite satellite) {
        SatelliteConstellation constellation = getOrThrow(constellationName);
        attached(constellation, satellite).takePhoto();
        repository.save(constellation);
    }

    public void sendData(String constellationName, CommunicationSatellite satellite, double dataSize) {
        SatelliteConstellation constellation = getOrThrow(constellationName);
        attached(constellation, satellite).sendData(dataSize);
        repository.save(constellation);
    }

    public void performSatelliteMission(String constellationName, Satellite satellite) {
        SatelliteConstellation constellation = getOrThrow(constellationName);
        attached(constellation, satellite).performMission();
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
                .orElseThrow(() -> new NotFoundException("Constellation not found: " + name));
    }

    /**
     * Callers usually pass a satellite read in an earlier transaction (a detached copy). Changes to that copy
     * are not saved, so we work with the managed instance from the constellation loaded in this transaction.
     */
    @SuppressWarnings("unchecked")
    private <T extends Satellite> T attached(SatelliteConstellation constellation, T satellite) {
        if (satellite.getId() == null) {
            return satellite;
        }
        for (Satellite managed : constellation.getSatellites()) {
            if (satellite.getId().equals(managed.getId())) {
                return (T) managed;
            }
        }
        return satellite;
    }
}
