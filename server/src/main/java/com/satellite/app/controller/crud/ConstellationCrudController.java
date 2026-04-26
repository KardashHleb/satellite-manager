package com.satellite.app.controller.crud;

import com.satellite.app.dto.crud.CreateConstellationRequest;
import com.satellite.app.dto.crud.RenameConstellationRequest;
import com.satellite.app.model.SatelliteConstellation;
import com.satellite.app.service.CrudManagementService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/crud/constellations")
public class ConstellationCrudController {

    private final CrudManagementService crudManagementService;

    public ConstellationCrudController(CrudManagementService crudManagementService) {
        this.crudManagementService = crudManagementService;
    }

    @GetMapping
    public List<SatelliteConstellation> list() {
        return crudManagementService.listConstellations();
    }

    @GetMapping("/{name}")
    public SatelliteConstellation get(@PathVariable String name) {
        return crudManagementService.getConstellation(name);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SatelliteConstellation create(@Valid @RequestBody CreateConstellationRequest body) {
        return crudManagementService.createConstellation(body);
    }

    @PutMapping("/{name}")
    public SatelliteConstellation rename(
            @PathVariable String name,
            @Valid @RequestBody RenameConstellationRequest body) {
        return crudManagementService.renameConstellation(name, body);
    }

    @DeleteMapping("/{name}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String name) {
        crudManagementService.deleteConstellation(name);
    }
}
