package com.satellite.app.repository;

import com.satellite.app.model.CommunicationSatellite;
import com.satellite.app.model.ImagingSatellite;
import com.satellite.app.model.Satellite;
import com.satellite.app.model.SatelliteConstellation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@DisplayName("JPA: репозиторий спутников и наследование")
class SatelliteRepositoryJpaTest {

    @Autowired
    private ConstellationRepository constellationRepository;

    @Autowired
    private SatelliteRepository satelliteRepository;

    @Test
    void persistJoinedInheritance_andFindByConstellation() {
        SatelliteConstellation c = new SatelliteConstellation("Fleet");
        c.addSatellite(new CommunicationSatellite("Comm-1", 0.8, 100.0));
        c.addSatellite(new ImagingSatellite("Img-1", 0.7, 2.5));
        constellationRepository.saveAndFlush(c);

        List<Satellite> sats = satelliteRepository.findByConstellation_ConstellationName("Fleet");
        assertEquals(2, sats.size());

        Optional<Satellite> img = satelliteRepository.findByConstellation_ConstellationNameAndName("Fleet", "Img-1");
        assertTrue(img.isPresent());
        assertTrue(img.get() instanceof ImagingSatellite);
        assertEquals(2.5, ((ImagingSatellite) img.get()).getResolution(), 0.001);
    }

    @Test
    void findDetailedById_loadsEnergyAndState() {
        SatelliteConstellation c = new SatelliteConstellation("Solo");
        c.addSatellite(new ImagingSatellite("X1", 0.5, 1.0));
        constellationRepository.saveAndFlush(c);

        Satellite persisted = satelliteRepository.findByConstellation_ConstellationNameAndName("Solo", "X1").orElseThrow();
        Long id = persisted.getId();

        Satellite loaded = satelliteRepository.findDetailedById(id).orElseThrow();
        assertNotNull(loaded.getEnergy());
        assertNotNull(loaded.getState());
        assertEquals(0.5, loaded.getBatteryLevel(), 0.01);
    }
}
