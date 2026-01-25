package com.satellite.app;

public class EnergySystem {
    private double batteryLevel;

    public EnergySystem(double batteryLevel) {
        this.batteryLevel = Math.max(0.0, Math.min(1.0, batteryLevel));
    }

    public double getBatteryLevel() {
        return batteryLevel;
    }

    public int getBatteryPercentage() {
        return (int)(batteryLevel * 100);
    }

    public void consumeBattery(double amount) {
        if (amount < 0) {
            System.out.println("Ошибка - значение расхода заряда не может быть отрицательным");
            return;
        }

        batteryLevel = Math.max(0, batteryLevel - amount);

        if (batteryLevel <= 0) {
            batteryLevel = 0;
            System.out.println("Батарея полностью разряжена");
        }
    }
}