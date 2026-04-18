package com.satellite.app.dto;

public class MissionRequest {
    private String targetType; // CONSTELLATION или SINGLE_SATELLITE
    private String constellationName;
    private String satelliteName; // обязательно для SINGLE_SATELLITE

    public MissionRequest() {}

    public MissionRequest(String targetType, String constellationName, String satelliteName) {
        this.targetType = targetType;
        this.constellationName = constellationName;
        this.satelliteName = satelliteName;
    }

    // Геттеры и сеттеры
    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public String getConstellationName() {
        return constellationName;
    }

    public void setConstellationName(String constellationName) {
        this.constellationName = constellationName;
    }

    public String getSatelliteName() {
        return satelliteName;
    }

    public void setSatelliteName(String satelliteName) {
        this.satelliteName = satelliteName;
    }
}