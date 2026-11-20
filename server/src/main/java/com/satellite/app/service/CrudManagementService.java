package com.satellite.app.service;

import com.satellite.app.EnergySystem;
import com.satellite.app.SatelliteState;
import com.satellite.app.config.CacheConfig;
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
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * CRUD operations with read caching. All cache invalidation goes through
 * {@link SatelliteCacheEvictionService}, which evicts after the transaction commits.
 */
@Service
@Transactional
public class CrudManagementService {

    private static final String CONSTELLATION_NOT_FOUND = "Constellation not found";
    private static final String SATELLITE_NOT_FOUND = "Satellite not found";

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
    @Cacheable(value = CacheConfig.CONSTELLATION, key = "#name")
    public SatelliteConstellation getConstellation(String name) {
        return constellationRepository.findByConstellationName(name)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, CONSTELLATION_NOT_FOUND));
    }

    public SatelliteConstellation createConstellation(CreateConstellationRequest request) {
        if (constellationRepository.existsByConstellationName(request.constellationName())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Constellation already exists");
        }
        return constellationRepository.save(new SatelliteConstellation(request.constellationName()));
    }

    public SatelliteConstellation renameConstellation(String currentName, RenameConstellationRequest request) {
        SatelliteConstellation c = constellationRepository.findByConstellationName(currentName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, CONSTELLATION_NOT_FOUND));
        if (constellationRepository.existsByConstellationName(request.newName())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Name is already taken");
        }
        c.setConstellationName(request.newName());
        SatelliteConstellation saved = constellationRepository.save(c);
        satelliteCacheEvictionService.constellationChanged(currentName, List.of());
        satelliteCacheEvictionService.constellationChanged(request.newName(), List.of());
        return saved;
    }

    public void deleteConstellation(String name) {
        SatelliteConstellation constellation = constellationRepository.findByConstellationName(name)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, CONSTELLATION_NOT_FOUND));
        List<Satellite> satellites = constellation.getSatellites();
        for (Satellite satellite : satellites) {
            satelliteEventPublisher.publishDeleted(satellite);
        }
        constellationRepository.deleteByConstellationName(name);
        satelliteCacheEvictionService.constellationChanged(name,
                satellites.stream().map(Satellite::getId).toList());
    }

    @Transactional(readOnly = true)
    public List<Satellite> listSatellites(String constellationName) {
        if (!constellationRepository.existsByConstellationName(constellationName)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, CONSTELLATION_NOT_FOUND);
        }
        return satelliteRepository.findByConstellation_ConstellationName(constellationName);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.SATELLITE, key = "#id")
    public Satellite getSatellite(Long id) {
        return satelliteRepository.findDetailedById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, SATELLITE_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.SATELLITES, key = "'" + CacheConfig.ALL_SATELLITES_KEY + "'")
    public List<Satellite> getAllSatellites() {
        return satelliteRepository.findAllDetailed();
    }

    /**
     * Not cached: an entry keyed by constellation and satellite name could not be evicted reliably
     * (the satellite id is not known when such an entry has to be invalidated).
     */
    @Transactional(readOnly = true)
    public Satellite findSatelliteByName(String constellationName, String satelliteName) {
        return satelliteRepository.findByConstellation_ConstellationNameAndName(constellationName, satelliteName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, SATELLITE_NOT_FOUND));
    }

    public void deleteSatellite(Long id) {
        Satellite satellite = satelliteRepository.findDetailedById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, SATELLITE_NOT_FOUND));
        String constellationName = satellite.getConstellation().getConstellationName();
        satelliteEventPublisher.publishDeleted(satellite);
        satelliteRepository.deleteById(id);
        satelliteCacheEvictionService.satelliteChanged(id, constellationName);
    }

    public Satellite addSatelliteFromRequest(AddSatelliteRequest request) {
        String constellationName = request.getConstellationName();
        String satelliteName = request.getSatelliteParam().getName();
        spaceOperationCenterService.addSatellite(request);
        Satellite saved = satelliteRepository
                .findByConstellation_ConstellationNameAndName(constellationName, satelliteName)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Satellite not found after saving"));
        satelliteCacheEvictionService.satelliteChanged(saved.getId(), constellationName);
        return saved;
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
        EnergySystem saved = energySystemRepository.save(e);
        evictSatellite(saved.getSatellite());
        return saved;
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
        SatelliteState saved = satelliteStateRepository.save(st);
        evictSatellite(saved.getSatellite());
        return saved;
    }

    private void evictSatellite(Satellite satellite) {
        satelliteCacheEvictionService.satelliteChanged(satellite.getId(),
                satellite.getConstellation().getConstellationName());
    }
}
