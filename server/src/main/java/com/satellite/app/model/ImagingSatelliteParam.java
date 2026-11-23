package com.satellite.app.model;

import com.satellite.app.model.enums.SatelliteType;

public class ImagingSatelliteParam extends SatelliteParam {
    private double resolution; // resolution in meters per pixel

    /**
     * For Jackson: the {@code type} discriminator in JSON has the same name as the field {@link SatelliteParam#type},
     * so the enum value in the field is often not filled in; the type is set explicitly.
     */
    public ImagingSatelliteParam() {
        super();
        setType(SatelliteType.IMAGE);
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