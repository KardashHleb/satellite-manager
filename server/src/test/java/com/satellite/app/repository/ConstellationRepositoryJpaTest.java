package com.satellite.app.repository;

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
@DisplayName("JPA: репозиторий группировок")
class ConstellationRepositoryJpaTest {

    @Autowired
    private ConstellationRepository constellationRepository;

    @Test
    void saveAndFindByConstellationName() {
        SatelliteConstellation c = new SatelliteConstellation("Alpha");
        constellationRepository.saveAndFlush(c);

        Optional<SatelliteConstellation> found = constellationRepository.findByConstellationName("Alpha");
        assertTrue(found.isPresent());
        assertEquals("Alpha", found.get().getConstellationName());
        assertTrue(constellationRepository.existsByConstellationName("Alpha"));
    }

    @Test
    void findAllWithSatellitesFetch_returnsSaved() {
        SatelliteConstellation c = new SatelliteConstellation("Beta");
        constellationRepository.saveAndFlush(c);

        List<SatelliteConstellation> all = constellationRepository.findAllWithSatellitesFetch();
        assertEquals(1, all.size());
        assertEquals("Beta", all.get(0).getConstellationName());
    }

    @Test
    void deleteByConstellationName() {
        constellationRepository.saveAndFlush(new SatelliteConstellation("Gamma"));
        assertTrue(constellationRepository.existsByConstellationName("Gamma"));

        constellationRepository.deleteByConstellationName("Gamma");

        assertFalse(constellationRepository.existsByConstellationName("Gamma"));
    }
}
