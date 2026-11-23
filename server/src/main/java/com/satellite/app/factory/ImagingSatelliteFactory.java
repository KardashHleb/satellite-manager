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
        // Check that the parameter has the expected type
        if (!(param instanceof ImagingSatelliteParam)) {
            throw new SpaceOperationException(
                    "Expected a parameter of type ImagingSatelliteParam, got: " +
                            param.getClass().getSimpleName()
            );
        }

        // Cast to the right type and extract the data
        ImagingSatelliteParam imagingParam = (ImagingSatelliteParam) param;

        // Create and return the satellite
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