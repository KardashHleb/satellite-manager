package com.satellite.app.model;

import com.satellite.app.model.enums.SatelliteType;

public abstract class SatelliteParam {
    protected SatelliteType type;
    protected String name;
    protected double batteryLevel;

    // Constructor
    public SatelliteParam(SatelliteType type, String name, double batteryLevel) {
        this.type = type;
        this.name = name;
        this.batteryLevel = batteryLevel;
    }

    // Getters and setters
    public SatelliteType getType() {
        return type;
    }

    public void setType(SatelliteType type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getBatteryLevel() {
        return batteryLevel;
    }

    public void setBatteryLevel(double batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    // Abstract method implemented by subclasses
    public abstract String getSpecialParameterInfo();

    @Override
    public String toString() {
        return String.format("Type: %s, Name: %s, Battery level: %.0f%%",
                type, name, batteryLevel * 100);
    }
}