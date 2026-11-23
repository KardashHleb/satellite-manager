package com.satellite.events;

/**
 * Satellite lifecycle topics (JSON messages {@link SatelliteLifecycleEvent}).
 */
public final class SatelliteKafkaTopics {

    public static final String CREATED = "satellite.created";
    public static final String DELETED = "satellite.deleted";

    private SatelliteKafkaTopics() {
    }
}
