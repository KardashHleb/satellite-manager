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
            System.out.println(getName() + ": imaging the area");
            takePhoto();
        } else {
            System.out.println(getName() + ": cannot perform the mission - satellite is off");
        }
    }

    public void takePhoto() {
        if (isActive() && getBatteryLevel() > 0.05) {
            getEnergy().consumeBattery(0.05);
            photosTaken++;
            System.out.println(getName() + ": photo taken. Resolution: " + resolution + " m/pixel. Total photos: " + photosTaken);

            if (getBatteryLevel() <= 0) {
                getState().deactivate();
                System.out.println(getName() + ": battery fully discharged, satellite turned off");
            }
        } else if (!isActive()) {
            System.out.println(getName() + ": cannot take a photo - satellite is off");
        } else {
            System.out.println(getName() + ": cannot take a photo - battery level is low");
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
