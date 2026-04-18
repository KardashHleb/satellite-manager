package com.satellite.missions.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class SpaceCenterGatewayService {

    private final RestClient spaceCenterRestClient;

    public SpaceCenterGatewayService(RestClient spaceCenterRestClient) {
        this.spaceCenterRestClient = spaceCenterRestClient;
    }

    public String fetchOverview() {
        return spaceCenterRestClient.get()
                .uri("/api/overview")
                .retrieve()
                .body(String.class);
    }
}
