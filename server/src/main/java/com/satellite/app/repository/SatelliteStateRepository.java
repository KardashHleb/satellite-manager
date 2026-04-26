package com.satellite.app.repository;

import com.satellite.app.SatelliteState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SatelliteStateRepository extends JpaRepository<SatelliteState, Long> {

    Optional<SatelliteState> findBySatellite_Id(Long satelliteId);
}
