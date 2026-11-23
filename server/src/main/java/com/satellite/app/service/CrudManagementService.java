package com.satellite.app.service;

import com.satellite.app.EnergySystem;
import com.satellite.app.SatelliteState;
import com.satellite.app.dto.crud.CreateConstellationRequest;
import com.satellite.app.dto.crud.RenameConstellationRequest;
import com.satellite.app.dto.crud.UpdateBatteryRequest;
import com.satellite.app.dto.crud.UpdateSatelliteStateRequest;
import com.satellite.app.model.Satellite;
import com.satellite.app.model.SatelliteConstellation;
import com.satellite.app.repository.ConstellationRepository;
import com.satellite.app.repository.EnergySystemRepository;
import com.satellite.app.repository.SatelliteRepository;
import com.satellite.app.kafka.SatelliteEventPublisher;
import com.satellite.app.repository.SatelliteStateRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class CrudManagementService {

    private final ConstellationRepository constellationRepository;
    private final SatelliteRepository satelliteRepository;
    private final EnergySystemRepository energySystemRepository;
    private final SatelliteStateRepository satelliteStateRepository;
    private final SpaceOperationCenterService spaceOperationCenterService;
    private final SatelliteEventPublisher satelliteEventPublisher;

    public CrudManagementService(ConstellationRepository constellationRepository,
                                 SatelliteRepository satelliteRepository,
                                 EnergySystemRepository energySystemRepository,
                                 SatelliteStateRepository satelliteStateRepository,
                                 SpaceOperationCenterService spaceOperationCenterService,
                                 SatelliteEventPublisher satelliteEventPublisher) {
        this.constellationRepository = constellationRepository;
        this.satelliteRepository = satelliteRepository;
        this.energySystemRepository = energySystemRepository;
        this.satelliteStateRepository = satelliteStateRepository;
        this.spaceOperationCenterService = spaceOperationCenterService;
        this.satelliteEventPublisher = satelliteEventPublisher;
    }

    @Transactional(readOnly = true)
    public List<SatelliteConstellation> listConstellations() {
        return constellationRepository.findAllWithSatellitesFetch();
    }

    @Transactional(readOnly = true)
    public SatelliteConstellation getConstellation(String name) {
        return constellationRepository.findByConstellationName(name)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Constellation not found"));
    }

    public SatelliteConstellation createConstellation(CreateConstellationRequest request) {
        if (constellationRepository.existsByConstellationName(request.constellationName())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Constellation already exists");
        }
        return constellationRepository.save(new SatelliteConstellation(request.constellationName()));
    }

    public SatelliteConstellation renameConstellation(String currentName, RenameConstellationRequest request) {
        SatelliteConstellation c = constellationRepository.findByConstellationName(currentName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Constellation not found"));
        if (constellationRepository.existsByConstellationName(request.newName())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Name is already taken");
        }
        c.setConstellationName(request.newName());
        return constellationRepository.save(c);
    }

    public void deleteConstellation(String name) {
        SatelliteConstellation constellation = constellationRepository.findByConstellationName(name)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Constellation not found"));
        for (Satellite satellite : constellation.getSatellites()) {
            satelliteEventPublisher.publishDeleted(satellite);
        }
        constellationRepository.deleteByConstellationName(name);
    }

    @Transactional(readOnly = true)
    public List<Satellite> listSatellites(String constellationName) {
        if (!constellationRepository.existsByConstellationName(constellationName)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Constellation not found");
        }
        return satelliteRepository.findByConstellation_ConstellationName(constellationName);
    }

    @Transactional(readOnly = true)
    public Satellite getSatellite(Long id) {
        return satelliteRepository.findDetailedById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Satellite not found"));
    }

    public void deleteSatellite(Long id) {
        Satellite satellite = satelliteRepository.findDetailedById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Satellite not found"));
        satelliteRepository.deleteById(id);
        satelliteEventPublisher.publishDeleted(satellite);
    }

    public Satellite addSatelliteFromRequest(AddSatelliteRequest request) {
        String constellationName = request.getConstellationName();
        String satelliteName = request.getSatelliteParam().getName();
        spaceOperationCenterService.addSatellite(request);
        // Read the saved satellite back: the object built from the request is not the persisted one and has no id
        return satelliteRepository
                .findByConstellation_ConstellationNameAndName(constellationName, satelliteName)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Satellite not found after saving"));
    }

    @Transactional(readOnly = true)
    public EnergySystem getEnergySystem(Long id) {
        return energySystemRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Energy system not found"));
    }

    public EnergySystem updateBattery(Long energySystemId, UpdateBatteryRequest request) {
        EnergySystem e = energySystemRepository.findById(energySystemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Energy system not found"));
        e.setBatteryLevel(request.batteryLevel());
        return energySystemRepository.save(e);
    }

    @Transactional(readOnly = true)
    public SatelliteState getSatelliteState(Long id) {
        return satelliteStateRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "State not found"));
    }

    public SatelliteState updateSatelliteState(Long stateId, UpdateSatelliteStateRequest request) {
        SatelliteState st = satelliteStateRepository.findById(stateId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "State not found"));
        if (Boolean.TRUE.equals(request.active())) {
            st.activate();
        } else {
            st.deactivate();
        }
        return satelliteStateRepository.save(st);
    }
}
