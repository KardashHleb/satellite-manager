package com.satellite.app.kafka;

import com.satellite.app.model.CommunicationSatellite;
import com.satellite.app.model.ImagingSatellite;
import com.satellite.app.model.Satellite;
import com.satellite.app.model.enums.SatelliteType;
import com.satellite.events.SatelliteLifecycleEvent;

public final class SatelliteEventMapper {

    private SatelliteEventMapper() {
    }

    public static SatelliteLifecycleEvent toCreated(Satellite satellite) {
        return SatelliteLifecycleEvent.created(
                satellite.getId(),
                satellite.getName(),
                satellite.getConstellation().getConstellationName(),
                resolveType(satellite)
        );
    }

    public static SatelliteLifecycleEvent toDeleted(Satellite satellite) {
        return SatelliteLifecycleEvent.deleted(
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
