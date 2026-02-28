package com.satellite.app.model;

import com.satellite.app.model.enums.SatelliteType;

public class ImagingSatelliteParam extends SatelliteParam {
    private double resolution; // разрешение в метрах на пиксель

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
        return String.format("Разрешение: %.2f м/пиксель", resolution);
    }

    @Override
    public String toString() {
        return super.toString() + ", " + getSpecialParameterInfo();
    }
}
