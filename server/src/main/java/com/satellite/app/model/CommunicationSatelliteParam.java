package com.satellite.app.model;

import com.satellite.app.model.enums.SatelliteType;

public class CommunicationSatelliteParam extends SatelliteParam {
    private double bandwidth; // пропускная способность в МГц

    /**
     * См. {@link ImagingSatelliteParam#ImagingSatelliteParam()} — то же для связи.
     */
    public CommunicationSatelliteParam() {
        super();
        setType(SatelliteType.COMMUNICATION);
    }

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
        return String.format("Пропускная способность: %.2f МГц", bandwidth);
    }

    @Override
    public String toString() {
        return super.toString() + ", " + getSpecialParameterInfo();
    }
}