package com.satellite.app.kafka;

import com.satellite.app.model.CommunicationSatellite;
import com.satellite.app.model.ImagingSatellite;
import com.satellite.app.model.Satellite;
import com.satellite.app.model.enums.SatelliteType;
import com.satellite.events.SatelliteLifecycleEvent;

import java.util.UUID;

public final class SatelliteEventMapper {

    private SatelliteEventMapper() {
    }

    public static SatelliteLifecycleEvent toCreated(Satellite satellite, UUID eventId) {
        return SatelliteLifecycleEvent.created(
                eventId,
                satellite.getId(),
                satellite.getName(),
                satellite.getConstellation().getConstellationName(),
                resolveType(satellite)
        );
    }

    public static SatelliteLifecycleEvent toDeleted(Satellite satellite, UUID eventId) {
        return SatelliteLifecycleEvent.deleted(
                eventId,
                satellite.getId(),
                satellite.getName(),
                satellite.getConstellation().getConstellationName(),
                resolveType(satellite)
        );
    }

    private static String resolveType(Satellite satellite) {
        if (satellite instanceof ImagingSatellite) {
            return SatelliteType.IMAGE.name();
        }
        if (satellite instanceof CommunicationSatellite) {
            return SatelliteType.COMMUNICATION.name();
        }
        throw new IllegalArgumentException("Неизвестный тип спутника: " + satellite.getClass().getSimpleName());
    }
}
