package com.satellite.missions.controller;

import com.satellite.missions.service.SpaceCenterGatewayService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class MissionGatewayController {

    private final SpaceCenterGatewayService gatewayService;

    public MissionGatewayController(SpaceCenterGatewayService gatewayService) {
        this.gatewayService = gatewayService;
    }

    @GetMapping("/remote/overview")
    public ResponseEntity<String> proxyOverview() {
        return ResponseEntity.ok(gatewayService.fetchOverview());
    }
}
