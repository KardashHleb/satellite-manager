package com.satellite.app;


import lombok.Getter;
import lombok.EqualsAndHashCode;

@Getter
@EqualsAndHashCode
public class EnergySystem {
    private double batteryLevel;

    // Приватный конструктор - доступ только через Builder
    private EnergySystem(double batteryLevel) {
        this.batteryLevel = batteryLevel;
    }
    public static EnergySystemBuilder builder() {
        return new EnergySystemBuilder();
    }
    // Внутренний статический класс Builder с валидацией
    public static class EnergySystemBuilder {
        private double batteryLevel = 1.0; // Значение по умолчанию

        public EnergySystemBuilder batteryLevel(double batteryLevel) {
            if (batteryLevel < 0.0 || batteryLevel > 1.0) {
                throw new IllegalArgumentException("Battery level must be between 0.0 and 1.0");
            }
            this.batteryLevel = batteryLevel;
            return this;
        }

        public EnergySystem build() {
            // Дополнительная валидация при сборке
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