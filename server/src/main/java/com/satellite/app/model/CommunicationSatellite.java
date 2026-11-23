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
@Table(name = "communication_satellite")
@DiscriminatorValue("COMMUNICATION")
@PrimaryKeyJoinColumn(name = "id")
public class CommunicationSatellite extends Satellite {

    @Column(nullable = false)
    private double bandwidth;

    @Column(nullable = false)
    private double dataSent;

    public CommunicationSatellite(String name, double batteryLevel, double bandwidth) {
        super(name, batteryLevel);
        this.bandwidth = bandwidth;
        this.dataSent = 0;
    }

    @Override
    public void performMission() {
        if (isActive()) {
            System.out.println(getName() + ": performing a communication mission");
            sendData(0.1);
        } else {
            System.out.println(getName() + ": cannot perform the mission - satellite is off");
        }
    }

    public void sendData(double dataSizeGB) {
        if (isActive() && getBatteryLevel() > 0.02) {
            double batteryConsumption = dataSizeGB * 0.1;
            batteryConsumption = Math.min(batteryConsumption, 0.5);

            getEnergy().consumeBattery(batteryConsumption);
            dataSent += dataSizeGB;

            System.out.println(getName() + ": sent " + dataSizeGB + " GB of data. "
                    + "Speed: " + bandwidth + " Mbit/s. Total sent: "
                    + String.format("%.2f", dataSent) + " GB");

            if (getBatteryLevel() <= 0) {
                getState().deactivate();
                System.out.println(getName() + ": battery fully discharged, satellite turned off");
            }
        } else if (!isActive()) {
            System.out.println(getName() + ": cannot send data - satellite is off");
        } else {
            System.out.println(getName() + ": cannot send data - battery level is low");
        }
    }

    public boolean establishConnection() {
        if (isActive() && getBatteryLevel() > 0.1) {
            System.out.println(getName() + ": connection established, ready for data transmission");
            return true;
        }
        System.out.println(getName() + ": could not establish a connection");
        return false;
    }
}
