package com.satellite.app.factory;

import com.satellite.app.CommunicationSatellite;
import com.satellite.app.Satellite;
import org.springframework.stereotype.Component;

@Component
public class CommunicationSatelliteFactory implements SatelliteFactory {

    @Override
    public Satellite createSatellite(String name, double batteryLevel) {
        // Значение bandwidth по умолчанию
        return new CommunicationSatellite(name, batteryLevel, 100.0);
    }

    // Специализированный метод с указанием bandwidth
    public CommunicationSatellite createWithBandwidth(String name, double batteryLevel, double bandwidth) {
        return new CommunicationSatellite(name, batteryLevel, bandwidth);
    }

    // Перегрузка с именем и батареей по умолчанию
    public CommunicationSatellite createWithBandwidth(String name, double bandwidth) {
        return new CommunicationSatellite(name, 100.0, bandwidth);
    }

    // Специализированный метод для создания спутника с уже установленными параметрами из вашего кода
    public CommunicationSatellite createFromExisting(String name, double batteryLevel, double bandwidth) {
        CommunicationSatellite satellite = new CommunicationSatellite(name, batteryLevel, bandwidth);
        // Здесь можно добавить любую дополнительную логику инициализации
        return satellite;
    }
}