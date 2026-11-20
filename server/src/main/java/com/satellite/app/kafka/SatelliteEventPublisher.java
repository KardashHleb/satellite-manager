package com.satellite.app.kafka;

import com.satellite.app.model.Satellite;

public interface SatelliteEventPublisher {

    void publishCreated(Satellite satellite);

    void publishDeleted(Satellite satellite);
}
