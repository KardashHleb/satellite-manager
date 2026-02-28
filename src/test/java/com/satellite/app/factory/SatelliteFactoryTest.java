package com.satellite.app.factory;


import com.satellite.app.CommunicationSatellite;
import com.satellite.app.ImagingSatellite;
import com.satellite.app.Satellite;
import com.satellite.app.model.CommunicationSatelliteParam;
import com.satellite.app.model.ImagingSatelliteParam;
import com.satellite.app.model.SatelliteParam;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SatelliteFactoryTest {

    @Test
    void commFactory_createsCommSatellite() {
        CommunicationSatelliteFactory factory = new CommunicationSatelliteFactory();
        SatelliteParam param = new CommunicationSatelliteParam("Comm", 0.75, 500.0);  // 75% → 0.75

        Satellite sat = factory.createSatelliteWithParameter(param);

        assertTrue(sat instanceof CommunicationSatellite);
        assertEquals("Comm", sat.getName());
        assertEquals(0.75, sat.getBatteryLevel());  // Проверяем 0.75
        assertEquals(500.0, ((CommunicationSatellite) sat).getBandwidth());
    }

    @Test
    void imagingFactory_createsImagingSatellite() {
        ImagingSatelliteFactory factory = new ImagingSatelliteFactory();
        SatelliteParam param = new ImagingSatelliteParam("Img", 0.85, 1.5);  // 85% → 0.85

        Satellite sat = factory.createSatelliteWithParameter(param);

        assertTrue(sat instanceof ImagingSatellite);
        assertEquals("Img", sat.getName());
        assertEquals(0.85, sat.getBatteryLevel());  // Проверяем 0.85
        assertEquals(1.5, ((ImagingSatellite) sat).getResolution());
    }
}