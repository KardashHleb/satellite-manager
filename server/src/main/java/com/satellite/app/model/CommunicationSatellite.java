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
            System.out.println(getName() + ": выполняет миссию связи");
            sendData(0.1);
        } else {
            System.out.println(getName() + ": невозможно выполнить миссию - спутник выключен");
        }
    }

    public void sendData(double dataSizeGB) {
        if (isActive() && getBatteryLevel() > 0.02) {
            double batteryConsumption = dataSizeGB * 0.1;
            batteryConsumption = Math.min(batteryConsumption, 0.5);

            getEnergy().consumeBattery(batteryConsumption);
            dataSent += dataSizeGB;

            System.out.println(getName() + ": отправлено " + dataSizeGB + " ГБ данных. "
                    + "Скорость: " + bandwidth + " Мбит/с. Всего отправлено: "
                    + String.format("%.2f", dataSent) + " ГБ");

            if (getBatteryLevel() <= 0) {
                getState().deactivate();
                System.out.println(getName() + ": батарея полностью разряжена, спутник выключен");
            }
        } else if (!isActive()) {
            System.out.println(getName() + ": невозможно отправить данные - спутник выключен");
        } else {
            System.out.println(getName() + ": невозможно отправить данные - низкий заряд батареи");
        }
    }

    public boolean establishConnection() {
        if (isActive() && getBatteryLevel() > 0.1) {
            System.out.println(getName() + ": соединение установлено, готов к передаче данных");
            return true;
        }
        System.out.println(getName() + ": не удалось установить соединение");
        return false;
    }
}
