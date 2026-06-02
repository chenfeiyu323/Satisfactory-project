package com.example.satisfactory.service;

import com.example.satisfactory.calculator.FactoryCalculationService;
import com.example.satisfactory.dto.BusLinePatchRequest;
import com.example.satisfactory.dto.BusLineRequest;
import com.example.satisfactory.dto.BusLineResponse;
import com.example.satisfactory.entity.BusLine;
import com.example.satisfactory.entity.Factory;
import com.example.satisfactory.entity.Material;
import com.example.satisfactory.exception.BadRequestException;
import com.example.satisfactory.exception.NotFoundException;
import com.example.satisfactory.repository.BusLineRepository;
import com.example.satisfactory.repository.MaterialRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class BusLineService {
    private final BusLineRepository busLineRepository;
    private final MaterialRepository materialRepository;
    private final FactoryService factoryService;
    private final FactoryCalculationService calculationService;

    public BusLineService(BusLineRepository busLineRepository, MaterialRepository materialRepository, FactoryService factoryService, @Lazy FactoryCalculationService calculationService) {
        this.busLineRepository = busLineRepository;
        this.materialRepository = materialRepository;
        this.factoryService = factoryService;
        this.calculationService = calculationService;
    }

    @Transactional(readOnly = true)
    public List<BusLineResponse> findByFactory(Long factoryId) {
        return busLineRepository.findByFactoryIdOrderBySortOrderAscIdAsc(factoryId).stream().map(this::toResponse).toList();
    }

    public BusLineResponse create(Long factoryId, BusLineRequest request) {
        Factory factory = factoryService.getFactory(factoryId);
        Material material = materialRepository.findById(request.materialId()).orElseThrow(() -> new NotFoundException("Material not found: " + request.materialId()));
        if (busLineRepository.findByFactoryIdAndMaterialId(factoryId, material.getId()).isPresent()) {
            throw new BadRequestException("This factory already has a bus line for material: " + material.getName());
        }
        BusLine line = new BusLine();
        line.setFactory(factory);
        line.setMaterial(material);
        line.setName(request.name() == null || request.name().isBlank() ? material.getName() : request.name());
        line.setDescription(request.description());
        line.setOffsetAmount(request.offsetAmount() == null ? 0.0 : request.offsetAmount());
        line.setVisibleToOtherFactories(Boolean.TRUE.equals(request.visibleToOtherFactories()));
        line.setExternalEnabled(Boolean.TRUE.equals(request.externalEnabled()));
        line.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
        line.setCollapsed(Boolean.TRUE.equals(request.collapsed()));
        line.setCreatedManually(request.createdManually() == null || request.createdManually());
        BusLine saved = busLineRepository.save(line);
        validateExternalEnabled(saved);
        return toResponse(saved);
    }

    public BusLineResponse update(Long id, BusLinePatchRequest request) {
        BusLine line = getBusLine(id);
        if (request.name() != null) line.setName(request.name());
        if (request.description() != null) line.setDescription(request.description());
        if (request.offsetAmount() != null) line.setOffsetAmount(request.offsetAmount());
        if (request.visibleToOtherFactories() != null) line.setVisibleToOtherFactories(request.visibleToOtherFactories());
        if (request.externalEnabled() != null) line.setExternalEnabled(request.externalEnabled());
        validateExternalEnabled(line);
        if (request.sortOrder() != null) line.setSortOrder(request.sortOrder());
        if (request.collapsed() != null) line.setCollapsed(request.collapsed());
        return toResponse(line);
    }

    public void delete(Long id) {
        if (!busLineRepository.existsById(id)) throw new NotFoundException("Bus line not found: " + id);
        busLineRepository.deleteById(id);
    }

    public BusLine ensureBusLine(Factory factory, Material material, boolean createdManually) {
        return busLineRepository.findByFactoryIdAndMaterialId(factory.getId(), material.getId()).orElseGet(() -> {
            BusLine line = new BusLine();
            line.setFactory(factory);
            line.setMaterial(material);
            line.setName(material.getName());
            line.setOffsetAmount(0.0);
            line.setCreatedManually(createdManually);
            return busLineRepository.save(line);
        });
    }

    private void validateExternalEnabled(BusLine line) {
        if (!line.isExternalEnabled()) return;
        double net = calculationService.calculateBusLineNet(line.getId());
        if (net <= 0.000001) {
            throw new BadRequestException("Bus line net value must be positive before external output can be enabled.");
        }
    }

    public BusLine getBusLine(Long id) {
        return busLineRepository.findById(id).orElseThrow(() -> new NotFoundException("Bus line not found: " + id));
    }

    public BusLineResponse toResponse(BusLine line) {
        return new BusLineResponse(
                line.getId(), line.getFactory().getId(), line.getMaterial().getId(), line.getMaterial().getName(), line.getMaterial().getMaterialType(),
                line.getName(), line.getDescription(), line.getOffsetAmount(), line.isVisibleToOtherFactories(), line.isExternalEnabled(),
                line.getSortOrder(), line.isCollapsed(), line.isCreatedManually()
        );
    }
}
