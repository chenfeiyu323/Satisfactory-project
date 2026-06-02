package com.example.satisfactory.controller;

import com.example.satisfactory.calculator.FactoryCalculationService;
import com.example.satisfactory.dto.*;
import com.example.satisfactory.service.FactoryService;
import com.example.satisfactory.service.SnapshotService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/factories")
public class FactoryController {
    private final FactoryService factoryService;
    private final FactoryCalculationService calculationService;
    private final SnapshotService snapshotService;

    public FactoryController(FactoryService factoryService, FactoryCalculationService calculationService, SnapshotService snapshotService) {
        this.factoryService = factoryService;
        this.calculationService = calculationService;
        this.snapshotService = snapshotService;
    }

    @GetMapping
    public List<FactoryResponse> list() {
        return factoryService.findAll();
    }

    @GetMapping("/{id}")
    public FactoryResponse get(@PathVariable Long id) {
        return factoryService.findOne(id);
    }

    @PostMapping
    public FactoryResponse create(@Valid @RequestBody FactoryRequest request) {
        return factoryService.create(request);
    }

    @PatchMapping("/{id}")
    public FactoryResponse update(@PathVariable Long id, @Valid @RequestBody FactoryRequest request) {
        return factoryService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public MessageResponse delete(@PathVariable Long id) {
        factoryService.delete(id);
        return new MessageResponse("Factory deleted.");
    }

    @GetMapping("/{id}/calculation")
    public FactoryCalculationDto calculation(@PathVariable Long id) {
        return calculationService.calculateFactory(id);
    }

    @PostMapping("/{id}/copy")
    public FactoryResponse copy(@PathVariable Long id) {
        return factoryService.toResponse(snapshotService.copyFactory(id));
    }

    @PostMapping("/{id}/snapshots")
    public SnapshotResponse snapshot(@PathVariable Long id, @Valid @RequestBody SnapshotRequest request) {
        return snapshotService.createSnapshot(id, request);
    }

    @GetMapping("/{id}/snapshots")
    public List<SnapshotResponse> snapshots(@PathVariable Long id) {
        return snapshotService.listSnapshots(id);
    }
}
