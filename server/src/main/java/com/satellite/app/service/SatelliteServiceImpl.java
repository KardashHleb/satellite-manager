package com.satellite.app.service;

import com.satellite.app.model.Satellite;
import com.satellite.app.exception.SpaceOperationException;
import com.satellite.app.factory.SatelliteFactory;
import com.satellite.app.model.SatelliteParam;
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
        // Выбираем фабрику полиморфно по параметрам.
        SatelliteFactory factory = factories.stream()
                .filter(f -> f.supports(param))
                .findFirst()
                .orElseThrow(() -> new SpaceOperationException(
                        "Не найдена фабрика для создания спутника по переданным параметрам"
                ));

        return factory.createSatelliteWithParameter(param);
    }
}