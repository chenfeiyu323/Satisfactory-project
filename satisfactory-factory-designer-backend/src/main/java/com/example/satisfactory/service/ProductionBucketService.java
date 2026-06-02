package com.example.satisfactory.service;

import com.example.satisfactory.dto.ProductionBucketRequest;
import com.example.satisfactory.dto.ProductionBucketResponse;
import com.example.satisfactory.entity.Factory;
import com.example.satisfactory.entity.ProductionBucket;
import com.example.satisfactory.entity.ProductionNode;
import com.example.satisfactory.exception.NotFoundException;
import com.example.satisfactory.repository.ProductionBucketRepository;
import com.example.satisfactory.repository.ProductionNodeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProductionBucketService {
    private final ProductionBucketRepository bucketRepository;
    private final FactoryService factoryService;
    private final ProductionNodeRepository nodeRepository;
    private final BusLineService busLineService;

    public ProductionBucketService(ProductionBucketRepository bucketRepository, FactoryService factoryService, ProductionNodeRepository nodeRepository, BusLineService busLineService) {
        this.bucketRepository = bucketRepository;
        this.factoryService = factoryService;
        this.nodeRepository = nodeRepository;
        this.busLineService = busLineService;
    }

    @Transactional(readOnly = true)
    public List<ProductionBucketResponse> findByFactory(Long factoryId) {
        return bucketRepository.findByFactoryIdOrderBySortOrderAscIdAsc(factoryId).stream().map(this::toResponse).toList();
    }

    public ProductionBucketResponse create(Long factoryId, ProductionBucketRequest request) {
        Factory factory = factoryService.getFactory(factoryId);
        ProductionBucket bucket = new ProductionBucket();
        bucket.setFactory(factory);
        apply(bucket, request);
        ProductionBucket saved = bucketRepository.save(bucket);
        syncBusLinesIfEnabled(saved);
        return toResponse(saved);
    }

    public ProductionBucketResponse update(Long bucketId, ProductionBucketRequest request) {
        ProductionBucket bucket = getBucket(bucketId);
        apply(bucket, request);
        syncBusLinesIfEnabled(bucket);
        return toResponse(bucket);
    }

    public void delete(Long bucketId) {
        if (!bucketRepository.existsById(bucketId)) throw new NotFoundException("Production bucket not found: " + bucketId);
        bucketRepository.deleteById(bucketId);
    }

    public ProductionBucket getBucket(Long id) {
        return bucketRepository.findById(id).orElseThrow(() -> new NotFoundException("Production bucket not found: " + id));
    }

    private void apply(ProductionBucket bucket, ProductionBucketRequest request) {
        bucket.setName(request.name());
        if (request.enabled() != null) bucket.setEnabled(request.enabled());
        bucket.setDescription(request.description());
        if (request.positionX() != null) bucket.setPositionX(request.positionX());
        if (request.positionY() != null) bucket.setPositionY(request.positionY());
        if (request.collapsed() != null) bucket.setCollapsed(request.collapsed());
        if (request.sortOrder() != null) bucket.setSortOrder(request.sortOrder());
    }

    private void syncBusLinesIfEnabled(ProductionBucket bucket) {
        if (!bucket.getFactory().isEnabled() || !bucket.isEnabled()) return;
        for (ProductionNode node : nodeRepository.findByBucketIdOrderBySortOrderAscIdAsc(bucket.getId())) {
            if (!node.isEnabled()) continue;
            node.getRecipe().getInputs().forEach(input -> busLineService.ensureBusLine(bucket.getFactory(), input.getMaterial(), false));
            node.getRecipe().getOutputs().forEach(output -> busLineService.ensureBusLine(bucket.getFactory(), output.getMaterial(), false));
        }
    }

    public ProductionBucketResponse toResponse(ProductionBucket bucket) {
        return new ProductionBucketResponse(
                bucket.getId(), bucket.getFactory().getId(), bucket.getName(), bucket.isEnabled(), bucket.getDescription(),
                bucket.getPositionX(), bucket.getPositionY(), bucket.isCollapsed(), bucket.getSortOrder()
        );
    }
}
