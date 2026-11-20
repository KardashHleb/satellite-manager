package com.satellite.app;


public class ImagingSatellite extends Satellite {
    private final double resolution;
    private int photosTaken;

    public ImagingSatellite(String name, double batteryLevel, double resolution) {
        super(name, batteryLevel);
        this.resolution = resolution;
        this.photosTaken = 0;
    }

    public double getResolution() {
        return resolution;
    }

    public int getPhotosTaken() {
        return photosTaken;
    }

    @Override
    public void performMission() {
        if (isActive()) {
            System.out.println(name + ": imaging the area");
            takePhoto();
        } else {
            System.out.println(name + ": cannot perform mission - satellite is inactive");
        }
    }

    public void takePhoto() {
        if (isActive() && getBatteryLevel() > 0.05) {
            energy.consumeBattery(0.05);
            photosTaken++;
            System.out.println(name + ": photo taken. Resolution: " + resolution + " m/pixel. Total photos: " + photosTaken);

            // Check whether the battery is empty after use
            if (getBatteryLevel() <= 0) {
                state.deactivate();
                System.out.println(name + ": battery fully discharged, satellite deactivated");
            }
        } else if (!isActive()) {
            System.out.println(name + ": cannot take a photo - satellite is inactive");
        } else {
            System.out.println(name + ": cannot take a photo - low battery level");
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