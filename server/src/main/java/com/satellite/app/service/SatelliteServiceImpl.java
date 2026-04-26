package com.satellite.app.service;

import com.satellite.app.model.Satellite;
import com.satellite.app.exception.SpaceOperationException;
import com.satellite.app.factory.SatelliteFactory;
import com.satellite.app.model.CommunicationSatelliteParam;
import com.satellite.app.model.ImagingSatelliteParam;
import com.satellite.app.model.SatelliteParam;
import com.satellite.app.model.enums.SatelliteType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SatelliteServiceImpl implements SatelliteService {

    private final List<SatelliteFactory> factories;

    @Autowired
    public SatelliteServiceImpl(List<SatelliteFactory> factories) {
        this.factories = factories;
    }

    @Override
    public Satellite createSatellite(SatelliteParam param) {
        SatelliteType type = param.getType();
        if (type == null) {
            if (param instanceof ImagingSatelliteParam) {
                type = SatelliteType.IMAGE;
            } else if (param instanceof CommunicationSatelliteParam) {
                type = SatelliteType.COMMUNICATION;
            }
        }
        final SatelliteType resolvedType = type;
        // Ищем первую подходящую фабрику
        SatelliteFactory factory = factories.stream()
                .filter(f -> f.isSatelliteTypeSupported(resolvedType))
                .findFirst()
                .orElseThrow(() -> new SpaceOperationException(
                        String.format("Не найдена фабрика для создания спутника типа: %s", resolvedType)
                ));

        // Создаем спутник с помощью найденной фабрики
        return factory.createSatelliteWithParameter(param);
    }
}