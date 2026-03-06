package com.satellite.app.service;

import com.satellite.app.CommunicationSatellite;
import com.satellite.app.ImagingSatellite;
import com.satellite.app.Satellite;
import com.satellite.app.SatelliteConstellation;
import com.satellite.app.model.SatelliteParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class SpaceOperationCenterService {

    private final ConstellationService constellationService;
    private final SatelliteService satelliteService;

    @Autowired
    public SpaceOperationCenterService(ConstellationService constellationService,
                                       SatelliteService satelliteService) {
        this.constellationService = constellationService;
        this.satelliteService = satelliteService;
    }

    /**
     * Создает спутник через SatelliteService.
     * Теперь Main может вызывать это через Фасад.
     */
    public Satellite createSatellite(SatelliteParam param) {
        return satelliteService.createSatellite(param);
    }

    /**
     * Добавление готового спутника в группировку.
     */
    public void addSatellite(String constellationName, Satellite satellite) {
        constellationService.addSatellite(constellationName, satellite);
    }

    /**
     * Добавление через Request (для инициализации)
     */
    public Satellite addSatellite(AddSatelliteRequest request) {
        String name = request.getConstellationName();
        if (constellationService.get(name).isEmpty()) {
            constellationService.create(name);
        }
        // Создаем спутник из параметров, лежащих в Request
        Satellite satellite = satelliteService.createSatellite(request.getSatelliteParam());
        constellationService.addSatellite(name, satellite);
        return satellite;
    }

    // --- МЕТОДЫ ПОИСКА И ФИЛЬТРАЦИИ ---

    public <T extends Satellite> List<T> getSatellitesByType(String name, Class<T> type) {
        return constellationService.getSatellitesByType(name, type);
    }

    public Optional<SatelliteConstellation> get(String name) {
        return constellationService.get(name);
    }

    public Optional<SatelliteConstellation> getConstellation(String name) {
        return constellationService.get(name);
    }

    // --- ОПЕРАЦИИ (ПЕРЕАДРЕСАЦИЯ) ---

    public String getStatus(String name) {
        return constellationService.getStatus(name);
    }

    public List<Satellite> getSatellites(String name) {
        return constellationService.getSatellites(name);
    }

    public Map<String, SatelliteConstellation> getAll() {
        return constellationService.getAll();
    }

    public void activateAll(String name) {
        constellationService.activateAll(name);
    }

    public void deactivateAll(String name) {
        constellationService.deactivateAll(name);
    }

    public void activateSatellite(String group, Satellite sat) {
        constellationService.activateSatellite(group, sat);
    }

    public void deactivateSatellite(String group, Satellite sat) {
        constellationService.deactivateSatellite(group, sat);
    }

    public void performSatelliteMission(String group, Satellite sat) {
        constellationService.performSatelliteMission(group, sat);
    }

    public void takePhoto(String group, ImagingSatellite sat) {
        constellationService.takePhoto(group, sat);
    }

    public void sendData(String group, CommunicationSatellite sat, double size) {
        constellationService.sendData(group, sat, size);
    }

    // --- ГРУППОВЫЕ МИССИИ ---

    public void executeMissions(String group) {
        constellationService.executeMissions(group);
    }

    public void executeCommunicationMissions(String group) {
        constellationService.getSatellitesByType(group, CommunicationSatellite.class)
                .stream().filter(Satellite::isActive).forEach(sat -> constellationService.performSatelliteMission(group, sat));
    }

    public void executeImagingMissions(String group) {
        constellationService.getSatellitesByType(group, ImagingSatellite.class)
                .stream().filter(Satellite::isActive).forEach(sat -> constellationService.performSatelliteMission(group, sat));
    }
}