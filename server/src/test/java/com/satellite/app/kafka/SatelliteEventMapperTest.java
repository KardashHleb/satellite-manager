package com.satellite.app.kafka;

import com.satellite.app.model.CommunicationSatellite;
import com.satellite.app.model.SatelliteConstellation;
import com.satellite.events.SatelliteLifecycleEvent;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SatelliteEventMapperTest {

    @Test
    void toCreated_mapsFields() {
        SatelliteConstellation constellation = new SatelliteConstellation("RU Basic");
        CommunicationSatellite satellite = new CommunicationSatellite("Comm-1", 0.8, 100.0);
        constellation.addSatellite(satellite);
        satellite.setId(42L);

        UUID eventId = UUID.randomUUID();
        SatelliteLifecycleEvent event = SatelliteEventMapper.toCreated(satellite, eventId);

        assertEquals(eventId.toString(), event.eventId());
        assertEquals(SatelliteLifecycleEvent.EVENT_CREATED, event.eventType());
        assertEquals(42L, event.satelliteId());
        assertEquals("Comm-1", event.satelliteName());
        assertEquals("RU Basic", event.constellationName());
        assertEquals("COMMUNICATION", event.satelliteType());
    }
}
