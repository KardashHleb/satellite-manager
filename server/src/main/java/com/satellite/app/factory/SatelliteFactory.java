package com.satellite.app.factory;

import com.satellite.app.model.enums.SatelliteType;
import com.satellite.app.model.Satellite;
import com.satellite.app.model.SatelliteParam;


public interface SatelliteFactory {
    /**
     * Creates a satellite from the given parameters
     * @param param object with the satellite parameters
     * @return the created satellite
     */
    Satellite createSatelliteWithParameter(SatelliteParam param);

    /**
     * Checks whether the factory can create satellites of the given type
     * @param type satellite type
     * @return true if the type is supported, otherwise false
     */
    boolean isSatelliteTypeSupported(SatelliteType type);
}