package com.satellite.app.factory;

import com.satellite.app.model.CommunicationSatellite;
import com.satellite.app.model.Satellite;
import com.satellite.app.exception.SpaceOperationException;
import com.satellite.app.model.CommunicationSatelliteParam;
import com.satellite.app.model.SatelliteParam;
import com.satellite.app.model.enums.SatelliteType;
import org.springframework.stereotype.Component;

@Component
public class CommunicationSatelliteFactory implements SatelliteFactory {

    @Override
    public boolean supports(SatelliteParam param) {
        return param instanceof CommunicationSatelliteParam;
    }

    @Override
    public Satellite createSatelliteWithParameter(SatelliteParam param) {
        // Проверяем, что параметр имеет ожидаемый тип
        if (!(param instanceof CommunicationSatelliteParam)) {
            throw new SpaceOperationException(
                    "Ожидался параметр типа CommunicationSatelliteParam, получен: " +
                            param.getClass().getSimpleName()
            );
        }

        // Приводим к нужному типу и извлекаем данные
        CommunicationSatelliteParam communicationParam = (CommunicationSatelliteParam) param;

        // Создаем и возвращаем спутник
        return new CommunicationSatellite(
                communicationParam.getName(),
                communicationParam.getBatteryLevel(),
                communicationParam.getBandwidth()
        );
    }

    @Override
    public boolean isSatelliteTypeSupported(SatelliteType type) {
        return type == SatelliteType.COMMUNICATION;
    }
}