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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
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
    private final SatelliteCacheEvictionService satelliteCacheEvictionService;

    public CrudManagementService(ConstellationRepository constellationRepository,
                                 SatelliteRepository satelliteRepository,
                                 EnergySystemRepository energySystemRepository,
                                 SatelliteStateRepository satelliteStateRepository,
                                 SpaceOperationCenterService spaceOperationCenterService,
                                 SatelliteEventPublisher satelliteEventPublisher,
                                 SatelliteCacheEvictionService satelliteCacheEvictionService) {
        this.constellationRepository = constellationRepository;
        this.satelliteRepository = satelliteRepository;
        this.energySystemRepository = energySystemRepository;
        this.satelliteStateRepository = satelliteStateRepository;
        this.spaceOperationCenterService = spaceOperationCenterService;
        this.satelliteEventPublisher = satelliteEventPublisher;
        this.satelliteCacheEvictionService = satelliteCacheEvictionService;
    }

    @Transactional(readOnly = true)
    public List<SatelliteConstellation> listConstellations() {
        return constellationRepository.findAllWithSatellitesFetch();
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "constellation", key = "#name")
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
        SatelliteConstellation constellation = constellationRepository.findByConstellationName(name)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Группировка не найдена"));
        for (Satellite satellite : constellation.getSatellites()) {
            satelliteEventPublisher.publishDeleted(satellite);
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
    @Cacheable(value = "satellite", key = "#id")
    public Satellite getSatellite(Long id) {
        return satelliteRepository.findDetailedById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Спутник не найден"));
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "satellites", key = "'all'")
    public List<Satellite> getAllSatellites() {
        return satelliteRepository.findAllDetailed();
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "satellite", key = "#constellationName + '::' + #satelliteName")
    public Satellite findSatelliteByName(String constellationName, String satelliteName) {
        return satelliteRepository.findByConstellation_ConstellationNameAndName(constellationName, satelliteName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Спутник не найден"));
    }

    @Caching(evict = {
            @CacheEvict(value = "satellite", key = "#id"),
            @CacheEvict(value = "satellites", allEntries = true)
    })
    public void deleteSatellite(Long id) {
        Satellite satellite = satelliteRepository.findDetailedById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Спутник не найден"));
        satelliteEventPublisher.publishDeleted(satellite);
        satelliteRepository.deleteById(id);
    }

    @CacheEvict(value = "satellites", allEntries = true)
    public Satellite addSatelliteFromRequest(AddSatelliteRequest request) {
        String constellationName = request.getConstellationName();
        String satelliteName = request.getSatelliteParam().getName();
        spaceOperationCenterService.addSatellite(request);
        return satelliteRepository
                .findByConstellation_ConstellationNameAndName(constellationName, satelliteName)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Спутник не найден после сохранения"));
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
        EnergySystem saved = energySystemRepository.save(e);
        satelliteCacheEvictionService.evictSatellite(saved.getSatellite().getId());
        return saved;
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
        SatelliteState saved = satelliteStateRepository.save(st);
        satelliteCacheEvictionService.evictSatellite(saved.getSatellite().getId());
        return saved;
    }
}
