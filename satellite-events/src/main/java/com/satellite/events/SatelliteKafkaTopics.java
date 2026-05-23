package com.satellite.events;

/**
 * Топики жизненного цикла спутника (JSON-сообщения {@link SatelliteLifecycleEvent}).
 */
public final class SatelliteKafkaTopics {

    public static final String CREATED = "satellite.created";
    public static final String DELETED = "satellite.deleted";

    private SatelliteKafkaTopics() {
    }
}
