package com.satellite.app.factory;

import com.satellite.app.model.enums.SatelliteType;
import com.satellite.app.Satellite;
import com.satellite.app.model.SatelliteParam;


public interface SatelliteFactory {
    /**
     * Создает спутник на основе переданных параметров
     * @param param объект с параметрами спутника
     * @return созданный спутник
     */
    Satellite createSatelliteWithParameter(SatelliteParam param);

    /**
     * Проверяет, поддерживает ли фабрика создание спутников данного типа
     * @param type тип спутника
     * @return true если тип поддерживается, иначе false
     */
    boolean isSatelliteTypeSupported(SatelliteType type);
}