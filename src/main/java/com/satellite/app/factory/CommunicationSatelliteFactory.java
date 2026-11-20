package com.satellite.app.factory;

import com.satellite.app.CommunicationSatellite;
import com.satellite.app.Satellite;
import org.springframework.stereotype.Component;

@Component
public class CommunicationSatelliteFactory implements SatelliteFactory {

    @Override
    public Satellite createSatellite(String name, double batteryLevel) {
        // Default bandwidth
        return new CommunicationSatellite(name, batteryLevel, 100.0);
    }

    // Specialized method with an explicit bandwidth
    public CommunicationSatellite createWithBandwidth(String name, double batteryLevel, double bandwidth) {
        return new CommunicationSatellite(name, batteryLevel, bandwidth);
    }

    // Overload with a name and the default battery level
    public CommunicationSatellite createWithBandwidth(String name, double bandwidth) {
        return new CommunicationSatellite(name, 1.0, bandwidth);
    }

    // Specialized method that creates a satellite with parameters already set in your code
    public CommunicationSatellite createFromExisting(String name, double batteryLevel, double bandwidth) {
        CommunicationSatellite satellite = new CommunicationSatellite(name, batteryLevel, bandwidth);
        // Any additional initialization logic can be added here
        return satellite;
    }
}