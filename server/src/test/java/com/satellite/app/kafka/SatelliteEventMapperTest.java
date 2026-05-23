package com.satellite.app.kafka;

import com.satellite.app.model.CommunicationSatellite;
import com.satellite.app.model.SatelliteConstellation;
import com.satellite.events.SatelliteLifecycleEvent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SatelliteEventMapperTest {

    @Test
    void toCreated_mapsFields() {
        SatelliteConstellation constellation = new SatelliteConstellation("RU Basic");
        CommunicationSatellite satellite = new CommunicationSatellite("Связь-1", 0.8, 100.0);
        constellation.addSatellite(satellite);
        satellite.setId(42L);

        SatelliteLifecycleEvent event = SatelliteEventMapper.toCreated(satellite);

        assertEquals(SatelliteLifecycleEvent.EVENT_CREATED, event.eventType());
        assertEquals(42L, event.satelliteId());
        assertEquals("Связь-1", event.satelliteName());
        assertEquals("RU Basic", event.constellationName());
        assertEquals("COMMUNICATION", event.satelliteType());
    }
}
