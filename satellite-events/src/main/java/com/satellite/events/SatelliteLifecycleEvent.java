package com.satellite.events;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.UUID;

/**
 * Событие появления или удаления спутника в системе (формат JSON в Kafka).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SatelliteLifecycleEvent(
        String eventId,
        String eventType,
        Long satelliteId,
        String satelliteName,
        String constellationName,
        String satelliteType,
        String occurredAt
) {

    public static final String EVENT_CREATED = "CREATED";
    public static final String EVENT_DELETED = "DELETED";

    public static SatelliteLifecycleEvent created(
            UUID eventId,
            Long satelliteId,
            String satelliteName,
            String constellationName,
            String satelliteType
    ) {
        return new SatelliteLifecycleEvent(
                eventId.toString(),
                EVENT_CREATED,
                satelliteId,
                satelliteName,
                constellationName,
                satelliteType,
                Instant.now().toString()
        );
    }

    public static SatelliteLifecycleEvent deleted(
            UUID eventId,
            Long satelliteId,
            String satelliteName,
            String constellationName,
            String satelliteType
    ) {
        return new SatelliteLifecycleEvent(
                eventId.toString(),
                EVENT_DELETED,
                satelliteId,
                satelliteName,
                constellationName,
                satelliteType,
                Instant.now().toString()
        );
    }
}
