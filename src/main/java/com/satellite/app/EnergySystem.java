package com.satellite.app;


import lombok.Getter;
import lombok.EqualsAndHashCode;

@Getter
@EqualsAndHashCode
public class EnergySystem {
    private double batteryLevel;

    // Private constructor - access only through the Builder
    private EnergySystem(double batteryLevel) {
        this.batteryLevel = batteryLevel;
    }
    public static EnergySystemBuilder builder() {
        return new EnergySystemBuilder();
    }
    // Inner static Builder class with validation
    public static class EnergySystemBuilder {
        private double batteryLevel = 1.0; // Default value

        public EnergySystemBuilder batteryLevel(double batteryLevel) {
            if (batteryLevel < 0.0 || batteryLevel > 1.0) {
                throw new IllegalArgumentException("Battery level must be between 0.0 and 1.0");
            }
            this.batteryLevel = batteryLevel;
            return this;
        }

        public EnergySystem build() {
            // Additional validation on build
            if (batteryLevel < 0.0 || batteryLevel > 1.0) {
                throw new IllegalStateException("Battery level must be between 0.0 and 1.0");
            }
            return new EnergySystem(batteryLevel);
        }
    }

    public int getBatteryPercentage() {
        return (int)(batteryLevel * 100);
    }

    public void consumeBattery(double amount) {
        if (amount < 0) {
            System.out.println("Error - battery consumption cannot be negative");
            return;
        }

        batteryLevel = Math.max(0, batteryLevel - amount);

        if (batteryLevel <= 0) {
            batteryLevel = 0;
            System.out.println("Battery fully discharged");
        }
    }
}