package com.satellite.app.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.satellite.app.model.enums.SatelliteType;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = CommunicationSatelliteParam.class, name = "COMMUNICATION"),
        @JsonSubTypes.Type(value = ImagingSatelliteParam.class, name = "IMAGE")
})
public abstract class SatelliteParam {
    protected SatelliteType type;
    protected String name;
    protected double batteryLevel;

    // Пустой конструктор для Jackson
    public SatelliteParam() {}

    // Конструктор
    public SatelliteParam(SatelliteType type, String name, double batteryLevel) {
        this.type = type;
        this.name = name;
        this.batteryLevel = batteryLevel;
    }

    // Геттеры и сеттеры
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

    // Абстрактный метод, который будут реализовывать наследники
    public abstract String getSpecialParameterInfo();

    @Override
    public String toString() {
        return String.format("Тип: %s, Название: %s, Заряд батареи: %.1f%%",
                type, name, batteryLevel);
    }
}