package com.satellite.app.service;

public class MissionRequest {
    private final String constellationName;
    private final MissionType missionType;

    // Перечисление типов миссий поможет избежать ошибок при вводе строк
    public enum MissionType {
        SCAN_TERRAIN,    // Для ImagingSatellite (зондирование)
        BROADCAST_DATA,  // Для CommunicationSatellite (связь)
        ORBIT_ADJUSTMENT // Общая задача для всех
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