package com.satellite.app.service;

import com.satellite.app.CommunicationSatellite;
import com.satellite.app.ImagingSatellite;
import com.satellite.app.Satellite;
import com.satellite.app.SatelliteConstellation;
import com.satellite.app.repository.ConstellationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class SpaceOperationCenterService {

    private final ConstellationRepository repository;

    @Autowired
    public SpaceOperationCenterService(ConstellationRepository repository) {
        this.repository = repository;
    }

    // CRUD операций
    public SatelliteConstellation create(String name) {
        if (repository.existsByName(name)) {
            throw new IllegalArgumentException("Группировка уже существует: " + name);
        }
        return repository.save(new SatelliteConstellation(name));
    }

    public Optional<SatelliteConstellation> get(String name) {
        return repository.findByName(name);
    }

    public Map<String, SatelliteConstellation> getAll() {
        return repository.findAll();
    }

    public void delete(String name) {
        repository.deleteByName(name);
    }

    // Операции со спутниками
    public void addSatellite(String constellationName, Satellite satellite) {
        SatelliteConstellation constellation = getOrThrow(constellationName);
        constellation.addSatellite(satellite);
        repository.update(constellation);
    }

    public void removeSatellite(String constellationName, Satellite satellite) {
        SatelliteConstellation constellation = getOrThrow(constellationName);
        constellation.removeSatellite(satellite);
        repository.update(constellation);
    }

    // Управление группировками
    public void executeMissions(String constellationName) {
        SatelliteConstellation constellation = getOrThrow(constellationName);
        constellation.executeAllMissions();
        repository.update(constellation);
    }

    public void activateSatellite(String constellationName, Satellite satellite) {
        SatelliteConstellation constellation = getOrThrow(constellationName);
        satellite.activate();
        repository.update(constellation); // Обновляем в репозитории
    }

    public void deactivateSatellite(String constellationName, Satellite satellite) {
        SatelliteConstellation constellation = getOrThrow(constellationName);
        satellite.deactivate();
        repository.update(constellation); // Обновляем в репозитории
    }

    public void takePhoto(String constellationName, ImagingSatellite satellite) {
        SatelliteConstellation constellation = getOrThrow(constellationName);
        satellite.takePhoto();
        repository.update(constellation);
    }

    public void sendData(String constellationName, CommunicationSatellite satellite, double dataSize) {
        SatelliteConstellation constellation = getOrThrow(constellationName);
        satellite.sendData(dataSize);
        repository.update(constellation);
    }

    public void performSatelliteMission(String constellationName, Satellite satellite) {
        SatelliteConstellation constellation = getOrThrow(constellationName);
        satellite.performMission();
        repository.update(constellation); // Обновляем в репозитории
    }

    public void activateAll(String constellationName) {
        SatelliteConstellation constellation = getOrThrow(constellationName);
        constellation.activateAllSatellites();
        repository.update(constellation);
    }

    public void deactivateAll(String constellationName) {
        SatelliteConstellation constellation = getOrThrow(constellationName);
        constellation.deactivateAllSatellites();
        repository.update(constellation);
    }

    public String getStatus(String constellationName) {
        return getOrThrow(constellationName).getStatusReport();
    }

    public List<Satellite> getSatellites(String constellationName) {
        return getOrThrow(constellationName).getSatellites();
    }

    public <T extends Satellite> List<T> getSatellitesByType(String constellationName, Class<T> type) {
        return getOrThrow(constellationName).getSatellitesByType(type);
    }

    public String getConstellationName(String constellationName) {
        return getOrThrow(constellationName).getConstellationName();
    }

    // Вспомогательные методы
    private SatelliteConstellation getOrThrow(String name) {
        return repository.findByName(name)
                .orElseThrow(() -> new IllegalArgumentException("Группировка не найдена: " + name));
    }
}