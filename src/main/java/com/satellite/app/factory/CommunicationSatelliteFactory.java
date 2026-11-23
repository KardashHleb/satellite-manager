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
    public Satellite createSatelliteWithParameter(SatelliteParam param) {
        // Check that the parameter has the expected type
        if (!(param instanceof CommunicationSatelliteParam)) {
            throw new SpaceOperationException(
                    "Expected a parameter of type CommunicationSatelliteParam, got: " +
                            param.getClass().getSimpleName()
            );
        }

        // Cast to the concrete type and read the data
        CommunicationSatelliteParam communicationParam = (CommunicationSatelliteParam) param;

        // Create and return the satellite
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