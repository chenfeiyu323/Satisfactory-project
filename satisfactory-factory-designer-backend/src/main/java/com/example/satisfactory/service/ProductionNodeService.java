package com.example.satisfactory.service;

import com.example.satisfactory.dto.ProductionNodeRequest;
import com.example.satisfactory.dto.ProductionNodeResponse;
import com.example.satisfactory.entity.ProductionBucket;
import com.example.satisfactory.entity.ProductionNode;
import com.example.satisfactory.entity.Recipe;
import com.example.satisfactory.exception.BadRequestException;
import com.example.satisfactory.exception.NotFoundException;
import com.example.satisfactory.repository.ProductionNodeRepository;
import com.example.satisfactory.repository.RecipeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProductionNodeService {
    private final ProductionNodeRepository nodeRepository;
    private final ProductionBucketService bucketService;
    private final RecipeRepository recipeRepository;
    private final BusLineService busLineService;

    public ProductionNodeService(ProductionNodeRepository nodeRepository, ProductionBucketService bucketService, RecipeRepository recipeRepository, BusLineService busLineService) {
        this.nodeRepository = nodeRepository;
        this.bucketService = bucketService;
        this.recipeRepository = recipeRepository;
        this.busLineService = busLineService;
    }

    @Transactional(readOnly = true)
    public List<ProductionNodeResponse> findByBucket(Long bucketId) {
        return nodeRepository.findByBucketIdOrderBySortOrderAscIdAsc(bucketId).stream().map(this::toResponse).toList();
    }

    public ProductionNodeResponse create(Long bucketId, ProductionNodeRequest request) {
        ProductionBucket bucket = bucketService.getBucket(bucketId);
        Recipe recipe = getRecipe(request.recipeId());
        ProductionNode node = new ProductionNode();
        node.setBucket(bucket);
        node.setRecipe(recipe);
        apply(node, request, false);
        ProductionNode saved = nodeRepository.save(node);
        syncBusLinesIfEnabled(saved);
        return toResponse(saved);
    }

    public ProductionNodeResponse update(Long id, ProductionNodeRequest request) {
        ProductionNode node = getNode(id);
        apply(node, request, true);
        syncBusLinesIfEnabled(node);
        return toResponse(node);
    }

    public void delete(Long id) {
        if (!nodeRepository.existsById(id)) throw new NotFoundException("Production node not found: " + id);
        nodeRepository.deleteById(id);
    }

    public ProductionNode getNode(Long id) {
        return nodeRepository.findById(id).orElseThrow(() -> new NotFoundException("Production node not found: " + id));
    }

    private Recipe getRecipe(Long id) {
        return recipeRepository.findById(id).orElseThrow(() -> new NotFoundException("Recipe not found: " + id));
    }

    private void apply(ProductionNode node, ProductionNodeRequest request, boolean allowNullRecipe) {
        if (request.recipeId() != null) node.setRecipe(getRecipe(request.recipeId()));
        else if (!allowNullRecipe) throw new BadRequestException("recipeId is required");
        if (request.enabled() != null) node.setEnabled(request.enabled());
        if (request.machineCount() != null) node.setMachineCount(request.machineCount());
        if (request.clockPercent() != null) node.setClockPercent(request.clockPercent());
        if (request.outputMultiplier() != null) node.setOutputMultiplier(request.outputMultiplier());
        if (request.name() != null) node.setName(request.name());
        if (request.positionX() != null) node.setPositionX(request.positionX());
        if (request.positionY() != null) node.setPositionY(request.positionY());
        if (request.sortOrder() != null) node.setSortOrder(request.sortOrder());
        validatePositive(node.getMachineCount(), "machineCount");
        validatePositive(node.getClockPercent(), "clockPercent");
        validatePositive(node.getOutputMultiplier(), "outputMultiplier");
    }

    private void validatePositive(Double value, String field) {
        if (value == null || value < 0) throw new BadRequestException(field + " must be >= 0");
    }

    private void syncBusLinesIfEnabled(ProductionNode node) {
        if (!node.getBucket().getFactory().isEnabled() || !node.getBucket().isEnabled() || !node.isEnabled()) return;
        node.getRecipe().getInputs().forEach(input -> busLineService.ensureBusLine(node.getBucket().getFactory(), input.getMaterial(), false));
        node.getRecipe().getOutputs().forEach(output -> busLineService.ensureBusLine(node.getBucket().getFactory(), output.getMaterial(), false));
    }

    public ProductionNodeResponse toResponse(ProductionNode node) {
        return new ProductionNodeResponse(
                node.getId(), node.getBucket().getId(), node.getRecipe().getId(), node.getRecipe().getName(), node.isEnabled(),
                node.getMachineCount(), node.getClockPercent(), node.getOutputMultiplier(), node.getName(),
                node.getPositionX(), node.getPositionY(), node.getSortOrder()
        );
    }
}
