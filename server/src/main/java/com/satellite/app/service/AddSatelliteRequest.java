package com.satellite.app.service;

import com.satellite.app.model.SatelliteParam;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

// Parameters for adding a satellite
public class AddSatelliteRequest {
    private String constellationName;
    private SatelliteParam satelliteParam;

    // Empty constructor for JSON deserialization
    public AddSatelliteRequest() {}

    // Constructor for internal use
    @JsonCreator
    public AddSatelliteRequest(
            @JsonProperty("constellationName") String constellationName,
            @JsonProperty("satelliteParam") SatelliteParam satelliteParam) {
        this.constellationName = constellationName;
        this.satelliteParam = satelliteParam;
    }

    public String getConstellationName() {
        return constellationName;
    }

    public void setConstellationName(String constellationName) {
        this.constellationName = constellationName;
    }

    public SatelliteParam getSatelliteParam() {
        return satelliteParam;
    }

    public void setSatelliteParam(SatelliteParam satelliteParam) {
        this.satelliteParam = satelliteParam;
    }
}