package com.satellite.app.dto;

public class MissionRequest {
    private MissionTargetType targetType;
    private String constellationName;
    private String satelliteName;

    public MissionRequest() {}

    public MissionRequest(MissionTargetType targetType, String constellationName, String satelliteName) {
        this.targetType = targetType;
        this.constellationName = constellationName;
        this.satelliteName = satelliteName;
    }

    public MissionTargetType getTargetType() {
        return targetType;
    }

    public void setTargetType(MissionTargetType targetType) {
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