package com.satellite.app.controller;

import com.satellite.app.dto.MissionRequest;
import com.satellite.app.model.ImagingSatellite;
import com.satellite.app.model.CommunicationSatellite;
import com.satellite.app.model.SatelliteConstellation;
import com.satellite.app.service.SpaceOperationCenterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class SpaceOperationController {

    private final SpaceOperationCenterService spaceOperationCenterService;

    @Autowired
    public SpaceOperationController(SpaceOperationCenterService spaceOperationCenterService) {
        this.spaceOperationCenterService = spaceOperationCenterService;
    }

    // Получить все группировки
    @GetMapping("/constellations")
    public ResponseEntity<Map<String, SatelliteConstellation>> getAllConstellations() {
        return ResponseEntity.ok(spaceOperationCenterService.getAll());
    }

    // Получить конкретную группировку
    @GetMapping("/constellations/{constellationName}")
    public ResponseEntity<SatelliteConstellation> getConstellation(@PathVariable String constellationName) {
        return spaceOperationCenterService.get(constellationName)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Добавить спутник (используя вашу DTO)
    @PostMapping("/add-satellites")
    public ResponseEntity<String> addSatellite(@RequestBody com.satellite.app.service.AddSatelliteRequest request) {
        try {
            spaceOperationCenterService.addSatellite(request);
            return ResponseEntity.ok("Спутник успешно добавлен в группировку " + request.getConstellationName());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Ошибка: " + e.getMessage());
        }
    }

    // Выполнить миссию
    @PostMapping("/missions")
    public ResponseEntity<String> executeMission(@RequestBody MissionRequest request) {
        try {
            spaceOperationCenterService.executeMission(request);
            if (request.getSatelliteName() != null && !request.getSatelliteName().isBlank()) {
                return ResponseEntity.ok("✅ Миссия для спутника " + request.getSatelliteName() + " выполнена");
            }
            return ResponseEntity.ok("✅ Миссия для группировки " + request.getConstellationName() + " выполнена");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("❌ Ошибка: " + e.getMessage());
        }
    }

    // Активировать спутник
    @PostMapping("/constellations/{constellationName}/satellites/{satelliteName}/activate")
    public ResponseEntity<String> activateSatellite(
            @PathVariable String constellationName,
            @PathVariable String satelliteName) {

        try {
            var satellite = spaceOperationCenterService.findSatellite(constellationName, satelliteName);
            spaceOperationCenterService.activateSatellite(constellationName, satellite);
            return ResponseEntity.ok("✅ Спутник " + satelliteName + " активирован");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("❌ Ошибка: " + e.getMessage());
        }
    }

    // Деактивировать спутник
    @PostMapping("/constellations/{constellationName}/satellites/{satelliteName}/deactivate")
    public ResponseEntity<String> deactivateSatellite(
            @PathVariable String constellationName,
            @PathVariable String satelliteName) {

        try {
            var satellite = spaceOperationCenterService.findSatellite(constellationName, satelliteName);
            spaceOperationCenterService.deactivateSatellite(constellationName, satellite);
            return ResponseEntity.ok("✅ Спутник " + satelliteName + " деактивирован");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("❌ Ошибка: " + e.getMessage());
        }
    }

    // Получить текстовый обзор
    @GetMapping("/overview")
    public ResponseEntity<String> getOverview() {
        StringBuilder overview = new StringBuilder("=== Space Operation Center Overview ===\n\n");

        var constellations = spaceOperationCenterService.getAll();
        for (var entry : constellations.entrySet()) {
            overview.append(entry.getValue().getStatusReport());
            overview.append("\n");
        }

        return ResponseEntity.ok(overview.toString());
    }

    // Сделать снимок (для спутников ДЗЗ)
    @PostMapping("/constellations/{constellationName}/satellites/{satelliteName}/photo")
    public ResponseEntity<String> takePhoto(
            @PathVariable String constellationName,
            @PathVariable String satelliteName) {

        try {
            var satellite = spaceOperationCenterService.findSatellite(constellationName, satelliteName);
            if (satellite instanceof ImagingSatellite imagingSat) {
                spaceOperationCenterService.takePhoto(constellationName, imagingSat);
                return ResponseEntity.ok("📸 Снимок сделан спутником " + satelliteName);
            } else {
                return ResponseEntity.badRequest().body("❌ Спутник " + satelliteName + " не является спутником ДЗЗ");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("❌ Ошибка: " + e.getMessage());
        }
    }

    // Передать данные (для спутников связи)
    @PostMapping("/constellations/{constellationName}/satellites/{satelliteName}/send-data")
    public ResponseEntity<String> sendData(
            @PathVariable String constellationName,
            @PathVariable String satelliteName,
            @RequestParam double dataSize) {

        try {
            var satellite = spaceOperationCenterService.findSatellite(constellationName, satelliteName);
            if (satellite instanceof CommunicationSatellite commSat) {
                spaceOperationCenterService.sendData(constellationName, commSat, dataSize);
                return ResponseEntity.ok("📡 Передано " + dataSize + " ГБ данных через спутник " + satelliteName);
            } else {
                return ResponseEntity.badRequest().body("❌ Спутник " + satelliteName + " не является спутником связи");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("❌ Ошибка: " + e.getMessage());
        }
    }

}