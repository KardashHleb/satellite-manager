package com.satellite.app.config;

import com.satellite.app.model.CommunicationSatellite;
import com.satellite.app.model.ImagingSatellite;
import com.satellite.app.model.Satellite;
import com.satellite.app.model.SatelliteConstellation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Redis cache values can be written and read back")
class CacheConfigSerializationTest {

    private final RedisSerializationContext.SerializationPair<Object> values =
            new CacheConfig().redisCacheConfiguration().getValueSerializationPair();

    private Object roundTrip(Object value) {
        return values.read(values.write(value));
    }

    @Test
    @DisplayName("satellite, satellite list and constellation survive a JSON round trip")
    void entitiesRoundTrip() {
        SatelliteConstellation constellation = new SatelliteConstellation("RU Basic");
        CommunicationSatellite comm = new CommunicationSatellite("Comm-1", 0.8, 100.0);
        ImagingSatellite imager = new ImagingSatellite("Imager-1", 0.5, 1.0);
        constellation.addSatellite(comm);
        constellation.addSatellite(imager);
        comm.setId(1L);
        imager.setId(2L);

        Satellite satellite = (Satellite) roundTrip(comm);
        assertInstanceOf(CommunicationSatellite.class, satellite);
        assertEquals(1L, satellite.getId());
        assertEquals(0.8, satellite.getBatteryLevel());

        @SuppressWarnings("unchecked")
        List<Satellite> list = (List<Satellite>) roundTrip(new ArrayList<>(List.of(comm, imager)));
        assertEquals(2, list.size());
        assertInstanceOf(ImagingSatellite.class, list.get(1));

        SatelliteConstellation back = (SatelliteConstellation) roundTrip(constellation);
        assertEquals("RU Basic", back.getConstellationName());
        assertEquals(2, back.getSatellites().size());
        assertEquals(0.5, back.getSatellites().get(1).getBatteryLevel());
    }
}
