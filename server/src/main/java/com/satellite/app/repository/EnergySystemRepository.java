package com.satellite.app.repository;

import com.satellite.app.EnergySystem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EnergySystemRepository extends JpaRepository<EnergySystem, Long> {

    Optional<EnergySystem> findBySatellite_Id(Long satelliteId);
}
