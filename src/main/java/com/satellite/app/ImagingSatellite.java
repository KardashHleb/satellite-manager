package com.satellite.app;

import lombok.Getter;
import lombok.ToString;
import lombok.EqualsAndHashCode;

@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ImagingSatellite extends Satellite {
    private final double resolution;
    private int photosTaken;

    public ImagingSatellite(String name, double batteryLevel, double resolution) {
        super(name, batteryLevel);
        this.resolution = resolution;
        this.photosTaken = 0;
    }

    @Override
    public void performMission() {
        if (isActive()) {
            System.out.println(name + ": выполняет съемку территории");
            takePhoto();
        } else {
            System.out.println(name + ": невозможно выполнить миссию - спутник выключен");
        }
    }

    public void takePhoto() {
        if (isActive() && getBatteryLevel() > 0.05) {
            energy.consumeBattery(0.05);
            photosTaken++;
            System.out.println(name + ": фото сделано. Разрешение: " + resolution + " м/пиксель. Всего фото: " + photosTaken);

            // Проверка разряда батареи после использования
            if (getBatteryLevel() <= 0) {
                state.deactivate();
                System.out.println(name + ": батарея полностью разряжена, спутник выключен");
            }
        } else if (!isActive()) {
            System.out.println(name + ": невозможно сделать фото - спутник выключен");
        } else {
            System.out.println(name + ": невозможно сделать фото - низкий заряд батареи");
        }
    }

    @Override
    public String toString() {
        return "ImagingSatellite{" +
                "name='" + name + '\'' +
                ", resolution=" + resolution +
                ", photosTaken=" + photosTaken +
                ", isActive=" + isActive() +
                ", batteryLevel=" + getBatteryLevel() +
                '}';
    }
}