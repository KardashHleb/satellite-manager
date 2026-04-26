package com.satellite.app.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.satellite.app.EnergySystem;
import com.satellite.app.SatelliteState;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"constellation", "energy", "state"})
@Entity
@Table(name = "satellite")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "dtype", discriminatorType = DiscriminatorType.STRING, length = 32)
public abstract class Satellite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    protected String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "constellation_id", nullable = false)
    @JsonBackReference("constellation-satellites")
    private SatelliteConstellation constellation;

    @OneToOne(mappedBy = "satellite", cascade = CascadeType.ALL, orphanRemoval = true, optional = false)
    private EnergySystem energy;

    @OneToOne(mappedBy = "satellite", cascade = CascadeType.ALL, orphanRemoval = true, optional = false)
    private SatelliteState state;

    protected Satellite(String name, double batteryLevel) {
        this.name = name;
        this.energy = new EnergySystem(this, batteryLevel);
        this.state = new SatelliteState(this, false);
        System.out.println("Создан спутник: " + name + " заряд: " + this.energy.getBatteryPercentage() + "%");
    }

    public abstract void performMission();

    public boolean activate() {
        if (energy.getBatteryLevel() > 0.2 && !state.isActive()) {
            state.activate();
            System.out.println(name + ": спутник включен");
            return true;
        }
        System.out.println(name + ": невозможно включить - низкий заряд батареи (" +
                energy.getBatteryPercentage() + "%)");
        return false;
    }

    public boolean isActive() {
        return state.isActive();
    }

    public double getBatteryLevel() {
        return energy != null ? energy.getBatteryLevel() : 0;
    }

    public void deactivate() {
        state.deactivate();
        System.out.println(name + ": спутник выключен");
    }
}
