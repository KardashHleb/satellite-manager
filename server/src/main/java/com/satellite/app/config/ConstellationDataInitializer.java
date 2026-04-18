package com.satellite.app.config;

import com.satellite.app.model.CommunicationSatelliteParam;
import com.satellite.app.model.ImagingSatelliteParam;
import com.satellite.app.service.AddSatelliteRequest;
import com.satellite.app.service.SpaceOperationCenterService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
@Profile("!test")
public class ConstellationDataInitializer implements ApplicationRunner {

    private final SpaceOperationCenterService spaceCenter;

    public ConstellationDataInitializer(SpaceOperationCenterService spaceCenter) {
        this.spaceCenter = spaceCenter;
    }

    @Override
    public void run(ApplicationArguments args) {
        spaceCenter.addSatellite(
                new AddSatelliteRequest("RU Basic", new CommunicationSatelliteParam("Связь-1", 0.85, 500.0))
        );
        spaceCenter.addSatellite(
                new AddSatelliteRequest("RU Basic", new CommunicationSatelliteParam("Связь-2", 0.75, 1000.0))
        );
        spaceCenter.addSatellite(
                new AddSatelliteRequest("RU Basic", new ImagingSatelliteParam("ДЗЗ-1", 0.92, 2.5))
        );
        spaceCenter.addSatellite(
                new AddSatelliteRequest("RU Basic", new ImagingSatelliteParam("ДЗЗ-2", 0.45, 1.0))
        );
        spaceCenter.addSatellite(
                new AddSatelliteRequest("RU Basic", new ImagingSatelliteParam("ДЗЗ-3", 0.15, 0.5))
        );
    }
}
