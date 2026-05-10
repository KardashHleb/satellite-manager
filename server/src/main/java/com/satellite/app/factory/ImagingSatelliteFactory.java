package com.satellite.app.factory;

import com.satellite.app.model.ImagingSatellite;
import com.satellite.app.model.Satellite;
import com.satellite.app.exception.SpaceOperationException;
import com.satellite.app.model.ImagingSatelliteParam;
import com.satellite.app.model.SatelliteParam;
import com.satellite.app.model.enums.SatelliteType;
import org.springframework.stereotype.Component;

@Component
public class ImagingSatelliteFactory implements SatelliteFactory {

    @Override
    public boolean supports(SatelliteParam param) {
        return param instanceof ImagingSatelliteParam;
    }

    @Override
    public Satellite createSatelliteWithParameter(SatelliteParam param) {
        // Проверяем, что параметр имеет ожидаемый тип
        if (!(param instanceof ImagingSatelliteParam)) {
            throw new SpaceOperationException(
                    "Ожидался параметр типа ImagingSatelliteParam, получен: " +
                            param.getClass().getSimpleName()
            );
        }

        // Приводим к нужному типу и извлекаем данные
        ImagingSatelliteParam imagingParam = (ImagingSatelliteParam) param;

        // Создаем и возвращаем спутник
        return new ImagingSatellite(
                imagingParam.getName(),
                imagingParam.getBatteryLevel(),
                imagingParam.getResolution()
        );
    }

    @Override
    public boolean isSatelliteTypeSupported(SatelliteType type) {
        return type == SatelliteType.IMAGE;
    }
}