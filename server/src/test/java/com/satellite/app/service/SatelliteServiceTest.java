package com.satellite.app.service;

import com.satellite.app.model.ImagingSatellite;
import com.satellite.app.model.Satellite;
import com.satellite.app.model.ImagingSatelliteParam;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class SatelliteServiceTest {

    @Autowired
    private SatelliteService satelliteService;

    @Test
    void createSatellite_WithImagingParams_ShouldCreateImagingSatellite() {
        ImagingSatelliteParam param = new ImagingSatelliteParam("GeoEye-1", 0.85, 0.41);

        Satellite result = satelliteService.createSatellite(param);

        assertNotNull(result);
        assertTrue(result instanceof ImagingSatellite);
        assertEquals(0.41, ((ImagingSatellite) result).getResolution());
    }
}