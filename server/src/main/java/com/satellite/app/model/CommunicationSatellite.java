package com.satellite.app.model;

import lombok.Getter;
import lombok.ToString;
import lombok.EqualsAndHashCode;

@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class CommunicationSatellite extends Satellite {
    private final double bandwidth; // bandwidth in Mbit/s
    private double dataSent;  // total data sent in GB

    public CommunicationSatellite(String name, double batteryLevel, double bandwidth) {
        super(name, batteryLevel);
        this.bandwidth = bandwidth;
        this.dataSent = 0;
    }

    @Override
    public void performMission() {
        if (isActive()) {
            System.out.println(name + ": performing a communication mission");
            // A mission automatically sends a small amount of data
            sendData(0.1); // send 100 MB
        } else {
            System.out.println(name + ": cannot perform the mission - satellite is off");
        }
    }

    public void sendData(double dataSizeGB) {
        if (isActive() && getBatteryLevel() > 0.02) {
            // Battery consumption depends on the data size
            double batteryConsumption = dataSizeGB * 0.1; // 10% of the charge per 1 GB
            batteryConsumption = Math.min(batteryConsumption, 0.5); // at most 50% at a time

            energy.consumeBattery(batteryConsumption);
            dataSent += dataSizeGB;

            System.out.println(name + ": sent " + dataSizeGB + " GB of data. " +
                    "Speed: " + bandwidth + " Mbit/s. Total sent: " +
                    String.format("%.2f", dataSent) + " GB");

            // Check whether the battery is empty after use
            if (getBatteryLevel() <= 0) {
                state.deactivate();
                System.out.println(name + ": battery fully discharged, satellite turned off");
            }
        } else if (!isActive()) {
            System.out.println(name + ": cannot send data - satellite is off");
        } else {
            System.out.println(name + ": cannot send data - battery level is low");
        }
    }

    // Extra method that checks the connection
    public boolean establishConnection() {
        if (isActive() && getBatteryLevel() > 0.1) {
            System.out.println(name + ": connection established, ready for data transmission");
            return true;
        }
        System.out.println(name + ": could not establish a connection");
        return false;
    }
}