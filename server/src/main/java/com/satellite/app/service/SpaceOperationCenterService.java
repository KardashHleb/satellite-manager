package com.satellite.app.service;

import com.satellite.app.dto.MissionRequest;
import com.satellite.app.dto.MissionTargetType;
import com.satellite.app.exception.NotFoundException;
import com.satellite.app.model.CommunicationSatellite;
import com.satellite.app.model.ImagingSatellite;
import com.satellite.app.model.Satellite;
import com.satellite.app.model.SatelliteConstellation;
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
     * Creates a satellite through SatelliteService.
     * Now Main can call this through the Facade.
     */
    public Satellite createSatellite(SatelliteParam param) {
        return satelliteService.createSatellite(param);
    }

    /**
     * Adds an existing satellite to a constellation.
     */
    public void addSatellite(String constellationName, Satellite satellite) {
        constellationService.addSatellite(constellationName, satellite);
    }

    /**
     * Adds a satellite from a Request (used for initialization)
     */
    public Satellite addSatellite(AddSatelliteRequest request) {
        String name = request.getConstellationName();
        if (constellationService.get(name).isEmpty()) {
            constellationService.create(name);
        }
        // Create the satellite from the parameters in the Request
        Satellite satellite = satelliteService.createSatellite(request.getSatelliteParam());
        constellationService.addSatellite(name, satellite);
        return satellite;
    }

    // --- SEARCH AND FILTERING ---

    public <T extends Satellite> List<T> getSatellitesByType(String name, Class<T> type) {
        return constellationService.getSatellitesByType(name, type);
    }

    public Optional<SatelliteConstellation> get(String name) {
        return constellationService.get(name);
    }

    public Optional<SatelliteConstellation> getConstellation(String name) {
        return constellationService.get(name);
    }

    // --- OPERATIONS (DELEGATION) ---

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

    public boolean activateSatellite(String group, Satellite sat) {
        return constellationService.activateSatellite(group, sat);
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

    // --- GROUP MISSIONS ---

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

    public void executeMission(MissionRequest request) {
        if (request.getTargetType() == MissionTargetType.CONSTELLATION) {
            executeMissions(request.getConstellationName());
            return;
        }

        if (request.getTargetType() == MissionTargetType.SINGLE_SATELLITE) {
            Satellite satellite = findSatellite(request.getConstellationName(), request.getSatelliteName());
            performSatelliteMission(request.getConstellationName(), satellite);
            return;
        }

        throw new IllegalArgumentException("Unknown target type: " + request.getTargetType());
    }

    public Satellite findSatellite(String constellationName, String satelliteName) {
        return getSatellites(constellationName)
                .stream()
                .filter(s -> s.getName().equals(satelliteName))
                .findFirst()
                .orElseThrow(() -> new NotFoundException(
                        "Satellite not found: " + satelliteName + " in constellation: " + constellationName
                ));
    }
}