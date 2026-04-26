package com.satellite.app.repository;

import com.satellite.app.EnergySystem;
import com.satellite.app.model.ImagingSatellite;
import com.satellite.app.model.SatelliteConstellation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@DisplayName("JPA: репозиторий энергосистем")
class EnergySystemRepositoryJpaTest {

    @Autowired
    private ConstellationRepository constellationRepository;

    @Autowired
    private EnergySystemRepository energySystemRepository;

    @Test
    void findBySatellite_Id() {
        SatelliteConstellation c = new SatelliteConstellation("E1");
        c.addSatellite(new ImagingSatellite("Sat-E", 0.66, 1.2));
        constellationRepository.saveAndFlush(c);

        Long satelliteId = constellationRepository.findByConstellationName("E1").orElseThrow()
                .getSatellites().get(0).getId();

        EnergySystem e = energySystemRepository.findBySatellite_Id(satelliteId).orElseThrow();
        assertEquals(0.66, e.getBatteryLevel(), 0.001);
    }
}
