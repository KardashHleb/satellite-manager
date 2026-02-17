package com.satellite.app.factory;

import com.satellite.app.Satellite;

public interface SatelliteFactory {
    Satellite createSatellite(String name, double batteryLevel);

    default Satellite createSatellite(String name) {
        return createSatellite(name, 100.0);
    }
}