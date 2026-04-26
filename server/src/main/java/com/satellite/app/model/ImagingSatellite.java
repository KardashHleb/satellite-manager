package com.satellite.app.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "imaging_satellite")
@DiscriminatorValue("IMAGE")
@PrimaryKeyJoinColumn(name = "id")
public class ImagingSatellite extends Satellite {

    @Column(nullable = false)
    private double resolution;

    @Column(nullable = false)
    private int photosTaken;

    public ImagingSatellite(String name, double batteryLevel, double resolution) {
        super(name, batteryLevel);
        this.resolution = resolution;
        this.photosTaken = 0;
    }

    @Override
    public void performMission() {
        if (isActive()) {
            System.out.println(getName() + ": выполняет съемку территории");
            takePhoto();
        } else {
            System.out.println(getName() + ": невозможно выполнить миссию - спутник выключен");
        }
    }

    public void takePhoto() {
        if (isActive() && getBatteryLevel() > 0.05) {
            getEnergy().consumeBattery(0.05);
            photosTaken++;
            System.out.println(getName() + ": фото сделано. Разрешение: " + resolution + " м/пиксель. Всего фото: " + photosTaken);

            if (getBatteryLevel() <= 0) {
                getState().deactivate();
                System.out.println(getName() + ": батарея полностью разряжена, спутник выключен");
            }
        } else if (!isActive()) {
            System.out.println(getName() + ": невозможно сделать фото - спутник выключен");
        } else {
            System.out.println(getName() + ": невозможно сделать фото - низкий заряд батареи");
        }
    }

    @Override
    public String toString() {
        return "ImagingSatellite{"
                + "name='" + getName() + '\''
                + ", resolution=" + resolution
                + ", photosTaken=" + photosTaken
                + ", isActive=" + isActive()
                + ", batteryLevel=" + getBatteryLevel()
                + '}';
    }
}
