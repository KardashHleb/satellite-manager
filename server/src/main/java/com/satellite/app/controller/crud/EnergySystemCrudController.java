package com.satellite.app.controller.crud;

import com.satellite.app.EnergySystem;
import com.satellite.app.dto.crud.UpdateBatteryRequest;
import com.satellite.app.service.CrudManagementService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/crud/energy-systems")
public class EnergySystemCrudController {

    private final CrudManagementService crudManagementService;

    public EnergySystemCrudController(CrudManagementService crudManagementService) {
        this.crudManagementService = crudManagementService;
    }

    @GetMapping("/{id}")
    public EnergySystem get(@PathVariable Long id) {
        return crudManagementService.getEnergySystem(id);
    }

    @PatchMapping("/{id}")
    public EnergySystem patchBattery(@PathVariable Long id, @Valid @RequestBody UpdateBatteryRequest body) {
        return crudManagementService.updateBattery(id, body);
    }
}
