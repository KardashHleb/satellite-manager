package com.satellite.app.repository;

import com.satellite.app.model.Satellite;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SatelliteRepository extends JpaRepository<Satellite, Long> {

    @EntityGraph(attributePaths = {"energy", "state", "constellation"})
    List<Satellite> findByConstellation_ConstellationName(String constellationName);

    @EntityGraph(attributePaths = {"energy", "state", "constellation"})
    Optional<Satellite> findByConstellation_ConstellationNameAndName(String constellationName, String name);

    @EntityGraph(attributePaths = {"energy", "state", "constellation"})
    @Query("select s from Satellite s where s.id = :id")
    Optional<Satellite> findDetailedById(@Param("id") Long id);
}
