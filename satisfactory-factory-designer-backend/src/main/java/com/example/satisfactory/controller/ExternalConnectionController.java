package com.example.satisfactory.controller;

import com.example.satisfactory.dto.*;
import com.example.satisfactory.service.ExternalConnectionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ExternalConnectionController {
    private final ExternalConnectionService connectionService;

    public ExternalConnectionController(ExternalConnectionService connectionService) {
        this.connectionService = connectionService;
    }

    @GetMapping("/bus-lines/{targetBusLineId}/available-external-sources")
    public List<ExternalSourceOptionResponse> availableSources(@PathVariable Long targetBusLineId) {
        return connectionService.availableSources(targetBusLineId);
    }

    @PostMapping("/external-connections")
    public ExternalConnectionResponse create(@Valid @RequestBody CreateExternalConnectionRequest request) {
        return connectionService.create(request);
    }

    @GetMapping("/factories/{factoryId}/external-connections")
    public List<ExternalConnectionResponse> intoFactory(@PathVariable Long factoryId) {
        return connectionService.findConnectionsIntoFactory(factoryId);
    }

    @DeleteMapping("/external-connections/{connectionId}")
    public MessageResponse delete(@PathVariable Long connectionId) {
        connectionService.delete(connectionId);
        return new MessageResponse("External connection removed.");
    }
}
