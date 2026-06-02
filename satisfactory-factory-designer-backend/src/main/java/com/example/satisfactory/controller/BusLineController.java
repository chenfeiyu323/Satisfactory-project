package com.example.satisfactory.controller;

import com.example.satisfactory.dto.BusLinePatchRequest;
import com.example.satisfactory.dto.BusLineRequest;
import com.example.satisfactory.dto.BusLineResponse;
import com.example.satisfactory.dto.MessageResponse;
import com.example.satisfactory.service.BusLineService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class BusLineController {
    private final BusLineService busLineService;

    public BusLineController(BusLineService busLineService) {
        this.busLineService = busLineService;
    }

    @GetMapping("/factories/{factoryId}/bus-lines")
    public List<BusLineResponse> list(@PathVariable Long factoryId) {
        return busLineService.findByFactory(factoryId);
    }

    @PostMapping("/factories/{factoryId}/bus-lines")
    public BusLineResponse create(@PathVariable Long factoryId, @Valid @RequestBody BusLineRequest request) {
        return busLineService.create(factoryId, request);
    }

    @PatchMapping("/bus-lines/{busLineId}")
    public BusLineResponse update(@PathVariable Long busLineId, @RequestBody BusLinePatchRequest request) {
        return busLineService.update(busLineId, request);
    }

    @DeleteMapping("/bus-lines/{busLineId}")
    public MessageResponse delete(@PathVariable Long busLineId) {
        busLineService.delete(busLineId);
        return new MessageResponse("Bus line deleted.");
    }
}
