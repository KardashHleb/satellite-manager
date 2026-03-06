package com.satellite.app.service;

import com.satellite.app.Satellite;
import com.satellite.app.model.SatelliteParam;

// Параметры для добавления спутника
public class AddSatelliteRequest {
    private final String constellationName;
    private final SatelliteParam satelliteParam;

    public AddSatelliteRequest(String constellationName, SatelliteParam satelliteParam) {
        this.constellationName = constellationName;
        this.satelliteParam = satelliteParam;
    }

    public String getConstellationName() { return constellationName; }
    public SatelliteParam getSatelliteParam() { return satelliteParam; }
}
