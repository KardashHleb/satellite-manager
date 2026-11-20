package com.satellite.app.repository;

import com.satellite.app.SatelliteState;
import com.satellite.app.model.CommunicationSatellite;
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
@DisplayName("JPA: репозиторий состояний спутника")
class SatelliteStateRepositoryJpaTest {

    @Autowired
    private ConstellationRepository constellationRepository;

    @Autowired
    private SatelliteStateRepository satelliteStateRepository;

    @Test
    void findBySatellite_Id_andUpdate() {
        SatelliteConstellation c = new SatelliteConstellation("StateFleet");
        c.addSatellite(new CommunicationSatellite("C-State", 0.9, 200.0));
        constellationRepository.saveAndFlush(c);

        Long satelliteId = constellationRepository.findByConstellationName("StateFleet").orElseThrow()
                .getSatellites().get(0).getId();

        SatelliteState st = satelliteStateRepository.findBySatellite_Id(satelliteId).orElseThrow();
        assertFalse(st.isActive());

        st.activate();
        satelliteStateRepository.saveAndFlush(st);

        SatelliteState reloaded = satelliteStateRepository.findBySatellite_Id(satelliteId).orElseThrow();
        assertTrue(reloaded.isActive());
    }
}
