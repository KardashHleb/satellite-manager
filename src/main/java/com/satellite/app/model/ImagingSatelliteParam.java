package com.satellite.app.model;

import com.satellite.app.model.enums.SatelliteType;

public class ImagingSatelliteParam extends SatelliteParam {
    private double resolution; // resolution in meters per pixel

    // No-arg constructor for Jackson
    public ImagingSatelliteParam() {
        super();
        this.type = SatelliteType.IMAGE; // the JSON "type" property is consumed by @JsonTypeInfo
    }

    public ImagingSatelliteParam(String name, double batteryLevel, double resolution) {
        super(SatelliteType.IMAGE, name, batteryLevel);
        this.resolution = resolution;
    }

    public double getResolution() {
        return resolution;
    }

    public void setResolution(double resolution) {
        this.resolution = resolution;
    }

    @Override
    public String getSpecialParameterInfo() {
        return String.format("Resolution: %.2f m/pixel", resolution);
    }

    @Override
    public String toString() {
        return super.toString() + ", " + getSpecialParameterInfo();
    }
}