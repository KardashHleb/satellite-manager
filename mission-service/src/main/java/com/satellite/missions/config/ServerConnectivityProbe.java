package com.satellite.missions.config;

import com.satellite.missions.service.SpaceCenterGatewayService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
@Profile("!test")
public class ServerConnectivityProbe implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ServerConnectivityProbe.class);

    private final SpaceCenterGatewayService gatewayService;

    public ServerConnectivityProbe(SpaceCenterGatewayService gatewayService) {
        this.gatewayService = gatewayService;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            String overview = gatewayService.fetchOverview();
            int len = Math.min(120, overview.length());
            log.info("Connection to the space operation center established. Start of /api/overview response: {}",
                    overview.substring(0, len).replace('\n', ' '));
        } catch (Exception e) {
            log.warn("Could not reach the space operation center at startup (server not ready yet?): {}", e.toString());
        }
    }
}
