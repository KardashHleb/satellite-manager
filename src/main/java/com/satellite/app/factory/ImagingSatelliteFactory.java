package com.satellite.app.factory;

import com.satellite.app.ImagingSatellite;
import com.satellite.app.Satellite;
import org.springframework.stereotype.Component;

@Component
public class ImagingSatelliteFactory implements SatelliteFactory {

    @Override
    public Satellite createSatellite(String name, double batteryLevel) {
        // Default resolution
        return new ImagingSatellite(name, batteryLevel, 1.0);
    }

    // Specialized method with an explicit resolution
    public ImagingSatellite createWithResolution(String name, double batteryLevel, double resolution) {
        return new ImagingSatellite(name, batteryLevel, resolution);
    }

    // Overload with a name and the default battery level
    public ImagingSatellite createWithResolution(String name, double resolution) {
        return new ImagingSatellite(name, 1.0, resolution);
    }

    // Specialized method that creates a satellite with parameters already set in your code
    public ImagingSatellite createFromExisting(String name, double batteryLevel, double resolution) {
        ImagingSatellite satellite = new ImagingSatellite(name, batteryLevel, resolution);
        // Any additional initialization logic can be added here
        return satellite;
    }
}