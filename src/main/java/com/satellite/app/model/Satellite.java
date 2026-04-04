package com.satellite.app.model;

import com.satellite.app.EnergySystem;
import com.satellite.app.SatelliteState;
import lombok.Getter;
import lombok.ToString;
import lombok.EqualsAndHashCode;

@Getter
@ToString
@EqualsAndHashCode
public abstract class Satellite {
    protected final String name;
    protected SatelliteState state;
    protected EnergySystem energy;

    public Satellite(String name, double batteryLevel) {
        this.name = name;
        this.energy = EnergySystem.builder()
                .batteryLevel(batteryLevel)  // Устанавливаем уровень заряда
                .build();
        this.state = new SatelliteState(false);
        System.out.println("Создан спутник: " + name + " заряд: " + energy.getBatteryPercentage() + "%");
    }

    // Абстрактный метод для выполнения миссии (будет реализован в наследниках)
    public abstract void performMission();

    // Метод для включения спутника
    public boolean activate() {
        if (energy.getBatteryLevel() > 0.2 && !state.isActive()) {
            state.activate();
            System.out.println(name + ": спутник включен");
            return true;
        } else {
            System.out.println(name + ": невозможно включить - низкий заряд батареи (" +
                    energy.getBatteryPercentage() + "%)");
            return false;
        }
    }
    public boolean isActive() {
        return state.isActive();
    }

    public double getBatteryLevel() {
        return energy.getBatteryLevel();
    }

    // Метод для выключения спутника
    public void deactivate() {
        state.deactivate();
        System.out.println(name + ": спутник выключен");
    }

}