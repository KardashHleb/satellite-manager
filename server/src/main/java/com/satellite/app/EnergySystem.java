package com.satellite.app;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.satellite.app.model.Satellite;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "energy_system")
public class EnergySystem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "satellite_id", nullable = false, unique = true)
    @JsonIgnore
    private Satellite satellite;

    @Column(name = "battery_level", nullable = false)
    private double batteryLevel;

    public EnergySystem(Satellite satellite, double batteryLevel) {
        if (batteryLevel < 0.0 || batteryLevel > 1.0) {
            throw new IllegalArgumentException("Battery level must be between 0.0 and 1.0");
        }
        this.satellite = satellite;
        this.batteryLevel = batteryLevel;
    }

    public int getBatteryPercentage() {
        return (int) (batteryLevel * 100);
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
