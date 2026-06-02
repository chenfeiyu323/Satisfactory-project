package com.example.satisfactory.service;

import com.example.satisfactory.dto.SnapshotRequest;
import com.example.satisfactory.dto.SnapshotResponse;
import com.example.satisfactory.entity.*;
import com.example.satisfactory.exception.NotFoundException;
import com.example.satisfactory.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class SnapshotService {
    private final FactoryRepository factoryRepository;
    private final FactorySnapshotRepository snapshotRepository;
    private final BusLineRepository busLineRepository;
    private final ProductionBucketRepository bucketRepository;
    private final ProductionNodeRepository nodeRepository;
    private final ObjectMapper objectMapper;

    public SnapshotService(FactoryRepository factoryRepository, FactorySnapshotRepository snapshotRepository, BusLineRepository busLineRepository, ProductionBucketRepository bucketRepository, ProductionNodeRepository nodeRepository, ObjectMapper objectMapper) {
        this.factoryRepository = factoryRepository;
        this.snapshotRepository = snapshotRepository;
        this.busLineRepository = busLineRepository;
        this.bucketRepository = bucketRepository;
        this.nodeRepository = nodeRepository;
        this.objectMapper = objectMapper;
    }

    public SnapshotResponse createSnapshot(Long factoryId, SnapshotRequest request) {
        Factory factory = getFactory(factoryId);
        String json = buildSnapshotJson(factory);
        FactorySnapshot snapshot = new FactorySnapshot();
        snapshot.setFactory(factory);
        snapshot.setName(request.name());
        snapshot.setSnapshotJson(json);
        return toResponse(snapshotRepository.save(snapshot));
    }

    @Transactional(readOnly = true)
    public List<SnapshotResponse> listSnapshots(Long factoryId) {
        return snapshotRepository.findByFactoryIdOrderByCreatedAtDesc(factoryId).stream().map(this::toResponse).toList();
    }

    public Factory copyFactory(Long factoryId) {
        Factory source = getFactory(factoryId);
        Factory copy = new Factory();
        copy.setName(source.getName() + " Copy");
        copy.setFactoryType(source.getFactoryType());
        copy.setEnabled(false);
        copy.setMaxBeltLevel(source.getMaxBeltLevel());
        copy.setMaxPipeLevel(source.getMaxPipeLevel());
        copy.setDescription(source.getDescription());
        Factory savedFactory = factoryRepository.save(copy);

        Map<Long, ProductionBucket> bucketMap = new HashMap<>();
        for (ProductionBucket sourceBucket : bucketRepository.findByFactoryIdOrderBySortOrderAscIdAsc(source.getId())) {
            ProductionBucket bucket = new ProductionBucket();
            bucket.setFactory(savedFactory);
            bucket.setName(sourceBucket.getName());
            bucket.setEnabled(sourceBucket.isEnabled());
            bucket.setDescription(sourceBucket.getDescription());
            bucket.setPositionX(sourceBucket.getPositionX());
            bucket.setPositionY(sourceBucket.getPositionY());
            bucket.setCollapsed(sourceBucket.isCollapsed());
            bucket.setSortOrder(sourceBucket.getSortOrder());
            bucketMap.put(sourceBucket.getId(), bucketRepository.save(bucket));
        }
        for (ProductionBucket sourceBucket : bucketRepository.findByFactoryIdOrderBySortOrderAscIdAsc(source.getId())) {
            for (ProductionNode sourceNode : nodeRepository.findByBucketIdOrderBySortOrderAscIdAsc(sourceBucket.getId())) {
                ProductionNode node = new ProductionNode();
                node.setBucket(bucketMap.get(sourceBucket.getId()));
                node.setRecipe(sourceNode.getRecipe());
                node.setEnabled(sourceNode.isEnabled());
                node.setMachineCount(sourceNode.getMachineCount());
                node.setClockPercent(sourceNode.getClockPercent());
                node.setOutputMultiplier(sourceNode.getOutputMultiplier());
                node.setName(sourceNode.getName());
                node.setPositionX(sourceNode.getPositionX());
                node.setPositionY(sourceNode.getPositionY());
                node.setSortOrder(sourceNode.getSortOrder());
                nodeRepository.save(node);
            }
        }
        for (BusLine sourceLine : busLineRepository.findByFactoryIdOrderBySortOrderAscIdAsc(source.getId())) {
            BusLine line = new BusLine();
            line.setFactory(savedFactory);
            line.setMaterial(sourceLine.getMaterial());
            line.setName(sourceLine.getName());
            line.setDescription(sourceLine.getDescription());
            line.setOffsetAmount(sourceLine.getOffsetAmount());
            line.setVisibleToOtherFactories(false);
            line.setExternalEnabled(false);
            line.setSortOrder(sourceLine.getSortOrder());
            line.setCollapsed(sourceLine.isCollapsed());
            line.setCreatedManually(sourceLine.isCreatedManually());
            busLineRepository.save(line);
        }
        return savedFactory;
    }

    private String buildSnapshotJson(Factory factory) {
        try {
            Map<String, Object> root = new LinkedHashMap<>();
            root.put("factoryId", factory.getId());
            root.put("name", factory.getName());
            root.put("factoryType", factory.getFactoryType().name());
            root.put("enabled", factory.isEnabled());
            root.put("maxBeltLevel", factory.getMaxBeltLevel());
            root.put("maxPipeLevel", factory.getMaxPipeLevel());
            root.put("description", factory.getDescription());
            root.put("busLines", busLineRepository.findByFactoryIdOrderBySortOrderAscIdAsc(factory.getId()).stream().map(this::busLineSnapshot).toList());

            List<Map<String, Object>> bucketList = new ArrayList<>();
            for (ProductionBucket bucket : bucketRepository.findByFactoryIdOrderBySortOrderAscIdAsc(factory.getId())) {
                Map<String, Object> b = new LinkedHashMap<>();
                b.put("id", bucket.getId());
                b.put("name", bucket.getName());
                b.put("enabled", bucket.isEnabled());
                b.put("description", bucket.getDescription());
                b.put("positionX", bucket.getPositionX());
                b.put("positionY", bucket.getPositionY());
                b.put("sortOrder", bucket.getSortOrder());
                b.put("nodes", nodeRepository.findByBucketIdOrderBySortOrderAscIdAsc(bucket.getId()).stream().map(this::nodeSnapshot).toList());
                bucketList.add(b);
            }
            root.put("buckets", bucketList);
            return objectMapper.writeValueAsString(root);
        } catch (Exception e) {
            throw new RuntimeException("Failed to build snapshot JSON: " + e.getMessage(), e);
        }
    }

    private Map<String, Object> busLineSnapshot(BusLine line) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", line.getId());
        map.put("materialId", line.getMaterial().getId());
        map.put("materialName", line.getMaterial().getName());
        map.put("name", line.getName());
        map.put("description", line.getDescription());
        map.put("offsetAmount", line.getOffsetAmount());
        map.put("visibleToOtherFactories", line.isVisibleToOtherFactories());
        map.put("externalEnabled", line.isExternalEnabled());
        map.put("sortOrder", line.getSortOrder());
        return map;
    }

    private Map<String, Object> nodeSnapshot(ProductionNode node) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", node.getId());
        map.put("recipeId", node.getRecipe().getId());
        map.put("recipeName", node.getRecipe().getName());
        map.put("enabled", node.isEnabled());
        map.put("machineCount", node.getMachineCount());
        map.put("clockPercent", node.getClockPercent());
        map.put("outputMultiplier", node.getOutputMultiplier());
        map.put("name", node.getName());
        map.put("positionX", node.getPositionX());
        map.put("positionY", node.getPositionY());
        map.put("sortOrder", node.getSortOrder());
        return map;
    }

    private Factory getFactory(Long factoryId) {
        return factoryRepository.findById(factoryId).orElseThrow(() -> new NotFoundException("Factory not found: " + factoryId));
    }

    private SnapshotResponse toResponse(FactorySnapshot snapshot) {
        return new SnapshotResponse(snapshot.getId(), snapshot.getFactory().getId(), snapshot.getName(), snapshot.getSnapshotJson(), snapshot.getCreatedAt());
    }
}
