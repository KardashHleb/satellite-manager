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

    public CrudManagementService(ConstellationRepository constellationRepository,
                               SatelliteRepository satelliteRepository,
                               EnergySystemRepository energySystemRepository,
                               SatelliteStateRepository satelliteStateRepository,
                               SpaceOperationCenterService spaceOperationCenterService) {
        this.constellationRepository = constellationRepository;
        this.satelliteRepository = satelliteRepository;
        this.energySystemRepository = energySystemRepository;
        this.satelliteStateRepository = satelliteStateRepository;
        this.spaceOperationCenterService = spaceOperationCenterService;
    }

    @Transactional(readOnly = true)
    public List<SatelliteConstellation> listConstellations() {
        return constellationRepository.findAllWithSatellitesFetch();
    }

    @Transactional(readOnly = true)
    public SatelliteConstellation getConstellation(String name) {
        return constellationRepository.findByConstellationName(name)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Группировка не найдена"));
    }

    public SatelliteConstellation createConstellation(CreateConstellationRequest request) {
        if (constellationRepository.existsByConstellationName(request.constellationName())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Группировка уже существует");
        }
        return constellationRepository.save(new SatelliteConstellation(request.constellationName()));
    }

    public SatelliteConstellation renameConstellation(String currentName, RenameConstellationRequest request) {
        SatelliteConstellation c = constellationRepository.findByConstellationName(currentName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Группировка не найдена"));
        if (constellationRepository.existsByConstellationName(request.newName())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Имя уже занято");
        }
        c.setConstellationName(request.newName());
        return constellationRepository.save(c);
    }

    public void deleteConstellation(String name) {
        if (!constellationRepository.existsByConstellationName(name)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Группировка не найдена");
        }
        constellationRepository.deleteByConstellationName(name);
    }

    @Transactional(readOnly = true)
    public List<Satellite> listSatellites(String constellationName) {
        if (!constellationRepository.existsByConstellationName(constellationName)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Группировка не найдена");
        }
        return satelliteRepository.findByConstellation_ConstellationName(constellationName);
    }

    @Transactional(readOnly = true)
    public Satellite getSatellite(Long id) {
        return satelliteRepository.findDetailedById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Спутник не найден"));
    }

    public void deleteSatellite(Long id) {
        if (!satelliteRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Спутник не найден");
        }
        satelliteRepository.deleteById(id);
    }

    public Satellite addSatelliteFromRequest(AddSatelliteRequest request) {
        return spaceOperationCenterService.addSatellite(request);
    }

    @Transactional(readOnly = true)
    public EnergySystem getEnergySystem(Long id) {
        return energySystemRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Энергосистема не найдена"));
    }

    public EnergySystem updateBattery(Long energySystemId, UpdateBatteryRequest request) {
        EnergySystem e = energySystemRepository.findById(energySystemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Энергосистема не найдена"));
        e.setBatteryLevel(request.batteryLevel());
        return energySystemRepository.save(e);
    }

    @Transactional(readOnly = true)
    public SatelliteState getSatelliteState(Long id) {
        return satelliteStateRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Состояние не найдено"));
    }

    public SatelliteState updateSatelliteState(Long stateId, UpdateSatelliteStateRequest request) {
        SatelliteState st = satelliteStateRepository.findById(stateId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Состояние не найдено"));
        if (Boolean.TRUE.equals(request.active())) {
            st.activate();
        } else {
            st.deactivate();
        }
        return satelliteStateRepository.save(st);
    }
}
