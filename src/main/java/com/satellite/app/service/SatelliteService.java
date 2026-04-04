package com.satellite.app.service;

import com.satellite.app.model.Satellite;
import com.satellite.app.model.SatelliteParam;

public interface SatelliteService {
    Satellite createSatellite(SatelliteParam param);
}
