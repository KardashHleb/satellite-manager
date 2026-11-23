package com.satellite.app.model;

import com.satellite.app.model.enums.SatelliteType;

public class CommunicationSatelliteParam extends SatelliteParam {
    private double bandwidth; // bandwidth in Mbit/s

    public CommunicationSatelliteParam(String name, double batteryLevel, double bandwidth) {
        super(SatelliteType.COMMUNICATION, name, batteryLevel);
        this.bandwidth = bandwidth;
    }

    public double getBandwidth() {
        return bandwidth;
    }

    public void setBandwidth(double bandwidth) {
        this.bandwidth = bandwidth;
    }

    @Override
    public String getSpecialParameterInfo() {
        return String.format("Bandwidth: %.2f Mbit/s", bandwidth);
    }

    @Override
    public String toString() {
        return super.toString() + ", " + getSpecialParameterInfo();
    }
}
