package com.satellite.missions.controller;

import com.satellite.missions.service.SpaceCenterGatewayService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {"http://localhost:8082", "http://127.0.0.1:8082"})
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
