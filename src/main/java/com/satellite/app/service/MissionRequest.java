package com.satellite.app.service;

public class MissionRequest {
    private final String constellationName;
    private final MissionType missionType;

    // An enum of mission types avoids typos in plain strings
    public enum MissionType {
        SCAN_TERRAIN,    // For ImagingSatellite (Earth observation)
        BROADCAST_DATA,  // For CommunicationSatellite (communication)
        ORBIT_ADJUSTMENT // Common task for all satellites
    }

    public MissionRequest(String constellationName, MissionType missionType) {
        this.constellationName = constellationName;
        this.missionType = missionType;
    }

    public String getConstellationName() {
        return constellationName;
    }

    public MissionType getMissionType() {
        return missionType;
    }
}