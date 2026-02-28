package com.satellite.app.service;

import com.satellite.app.Satellite;
import com.satellite.app.model.SatelliteParam;

public interface SatelliteService {
    Satellite createSatellite(SatelliteParam param);
}
