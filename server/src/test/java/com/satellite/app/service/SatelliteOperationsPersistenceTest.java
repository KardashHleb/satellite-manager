package com.satellite.app.service;

import com.satellite.app.model.CommunicationSatelliteParam;
import com.satellite.app.model.ImagingSatellite;
import com.satellite.app.model.ImagingSatelliteParam;
import com.satellite.app.model.Satellite;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Satellite operations are saved to the database")
class SatelliteOperationsPersistenceTest {

    private static final String OPERATIONS_CONSTELLATION = "PersistenceTest-Operations";
    private static final String CRUD_CONSTELLATION = "PersistenceTest-Crud";

    @Autowired
    private SpaceOperationCenterService spaceCenter;

    @Autowired
    private CrudManagementService crudManagementService;

    @AfterEach
    void cleanUp() {
        for (String name : new String[]{OPERATIONS_CONSTELLATION, CRUD_CONSTELLATION}) {
            if (spaceCenter.get(name).isPresent()) {
                crudManagementService.deleteConstellation(name);
            }
        }
    }

    @Test
    @DisplayName("activate and takePhoto on a satellite read earlier (detached) are saved")
    void operationsOnDetachedSatellite_AreSaved() {
        spaceCenter.addSatellite(new AddSatelliteRequest(OPERATIONS_CONSTELLATION,
                new ImagingSatelliteParam("Imager-P", 0.9, 1.0)));

        Satellite detached = spaceCenter.getSatellites(OPERATIONS_CONSTELLATION).get(0);
        spaceCenter.activateSatellite(OPERATIONS_CONSTELLATION, detached);

        ImagingSatellite afterActivate = (ImagingSatellite) spaceCenter.getSatellites(OPERATIONS_CONSTELLATION).get(0);
        assertTrue(afterActivate.isActive(), "The activation should be saved");

        spaceCenter.takePhoto(OPERATIONS_CONSTELLATION, afterActivate);

        ImagingSatellite afterPhoto = (ImagingSatellite) spaceCenter.getSatellites(OPERATIONS_CONSTELLATION).get(0);
        assertEquals(1, afterPhoto.getPhotosTaken(), "The photo counter should be saved");
        assertEquals(0.85, afterPhoto.getBatteryLevel(), 1e-9, "The battery consumption should be saved");
    }

    @Test
    @DisplayName("CRUD create returns the saved satellite with its database id")
    void crudCreate_ReturnsSatelliteWithId() {
        Satellite created = crudManagementService.addSatelliteFromRequest(new AddSatelliteRequest(CRUD_CONSTELLATION,
                new CommunicationSatelliteParam("Comm-P", 0.8, 100.0)));

        assertNotNull(created.getId(), "The returned satellite should have an id");
    }
}
