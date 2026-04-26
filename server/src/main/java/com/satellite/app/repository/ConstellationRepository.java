package com.satellite.app.repository;

import com.satellite.app.model.SatelliteConstellation;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ConstellationRepository extends JpaRepository<SatelliteConstellation, Long> {

    @EntityGraph(attributePaths = {"satellites", "satellites.energy", "satellites.state"})
    Optional<SatelliteConstellation> findByConstellationName(String constellationName);

    @EntityGraph(attributePaths = {"satellites", "satellites.energy", "satellites.state"})
    @Query("select distinct c from SatelliteConstellation c")
    List<SatelliteConstellation> findAllWithSatellitesFetch();

    boolean existsByConstellationName(String name);

    void deleteByConstellationName(String name);
}
