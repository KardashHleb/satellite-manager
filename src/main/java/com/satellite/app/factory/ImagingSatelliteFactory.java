package com.satellite.app.factory;

import com.satellite.app.ImagingSatellite;
import com.satellite.app.Satellite;
import org.springframework.stereotype.Component;

@Component
public class ImagingSatelliteFactory implements SatelliteFactory {

    @Override
    public Satellite createSatellite(String name, double batteryLevel) {
        // Значение resolution по умолчанию
        return new ImagingSatellite(name, batteryLevel, 1.0);
    }

    // Специализированный метод с указанием resolution
    public ImagingSatellite createWithResolution(String name, double batteryLevel, double resolution) {
        return new ImagingSatellite(name, batteryLevel, resolution);
    }

    // Перегрузка с именем и батареей по умолчанию
    public ImagingSatellite createWithResolution(String name, double resolution) {
        return new ImagingSatellite(name, 100.0, resolution);
    }

    // Специализированный метод для создания спутника с уже установленными параметрами из вашего кода
    public ImagingSatellite createFromExisting(String name, double batteryLevel, double resolution) {
        ImagingSatellite satellite = new ImagingSatellite(name, batteryLevel, resolution);
        // Здесь можно добавить любую дополнительную логику инициализации
        return satellite;
    }
}