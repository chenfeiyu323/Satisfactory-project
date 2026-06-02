package com.example.satisfactory.sync;

import com.example.satisfactory.dto.MessageResponse;
import com.example.satisfactory.entity.BusLine;
import com.example.satisfactory.entity.ExternalConnection;
import com.example.satisfactory.entity.Factory;
import com.example.satisfactory.entity.Material;
import com.example.satisfactory.entity.ProductionBucket;
import com.example.satisfactory.entity.ProductionNode;
import com.example.satisfactory.entity.Recipe;
import com.example.satisfactory.exception.BadRequestException;
import com.example.satisfactory.repository.BusLineRepository;
import com.example.satisfactory.repository.ExternalConnectionRepository;
import com.example.satisfactory.repository.FactoryRepository;
import com.example.satisfactory.repository.MaterialRepository;
import com.example.satisfactory.repository.ProductionBucketRepository;
import com.example.satisfactory.repository.ProductionNodeRepository;
import com.example.satisfactory.repository.RecipeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class SaveSyncService {
    private static final int SAVE_VERSION = 1;

    private final ObjectMapper objectMapper;
    private final FactoryRepository factoryRepository;
    private final ProductionBucketRepository bucketRepository;
    private final ProductionNodeRepository nodeRepository;
    private final BusLineRepository busLineRepository;
    private final ExternalConnectionRepository externalConnectionRepository;
    private final MaterialRepository materialRepository;
    private final RecipeRepository recipeRepository;

    public SaveSyncService(ObjectMapper objectMapper,
                           FactoryRepository factoryRepository,
                           ProductionBucketRepository bucketRepository,
                           ProductionNodeRepository nodeRepository,
                           BusLineRepository busLineRepository,
                           ExternalConnectionRepository externalConnectionRepository,
                           MaterialRepository materialRepository,
                           RecipeRepository recipeRepository) {
        this.objectMapper = objectMapper;
        this.factoryRepository = factoryRepository;
        this.bucketRepository = bucketRepository;
        this.nodeRepository = nodeRepository;
        this.busLineRepository = busLineRepository;
        this.externalConnectionRepository = externalConnectionRepository;
        this.materialRepository = materialRepository;
        this.recipeRepository = recipeRepository;
    }

    @Transactional(readOnly = true)
    public SaveFile exportSave() {
        List<FactoryExport> factories = new ArrayList<>();
        List<ExternalConnectionExport> connections = new ArrayList<>();
        Map<Long, String> busLineRefById = new LinkedHashMap<>();

        for (Factory factory : factoryRepository.findAll()) {
            String factoryRef = "factory-" + factory.getId();
            List<ProductionBucketExport> buckets = new ArrayList<>();
            List<BusLineExport> busLines = new ArrayList<>();

            for (ProductionBucket bucket : bucketRepository.findByFactoryIdOrderBySortOrderAscIdAsc(factory.getId())) {
                List<ProductionNodeExport> nodes = nodeRepository.findByBucketIdOrderBySortOrderAscIdAsc(bucket.getId())
                        .stream()
                        .map(node -> new ProductionNodeExport(
                                node.getRecipe().getGameKey(),
                                node.isEnabled(),
                                node.getMachineCount(),
                                node.getClockPercent(),
                                node.getOutputMultiplier(),
                                node.getName(),
                                node.getPositionX(),
                                node.getPositionY(),
                                node.getSortOrder()))
                        .toList();

                buckets.add(new ProductionBucketExport(
                        bucket.getName(),
                        bucket.isEnabled(),
                        bucket.getDescription(),
                        bucket.getPositionX(),
                        bucket.getPositionY(),
                        bucket.isCollapsed(),
                        bucket.getSortOrder(),
                        nodes));
            }

            for (BusLine line : busLineRepository.findByFactoryIdOrderBySortOrderAscIdAsc(factory.getId())) {
                String ref = factoryRef + "-bus-" + line.getId();
                busLineRefById.put(line.getId(), ref);
                busLines.add(new BusLineExport(
                        ref,
                        line.getMaterial().getGameKey(),
                        line.getName(),
                        line.getDescription(),
                        line.getOffsetAmount(),
                        line.isVisibleToOtherFactories(),
                        line.isExternalEnabled(),
                        line.getSortOrder(),
                        line.isCollapsed(),
                        line.isCreatedManually()));
            }

            factories.add(new FactoryExport(
                    factoryRef,
                    factory.getName(),
                    factory.getFactoryType().name(),
                    factory.isEnabled(),
                    factory.getMaxBeltLevel(),
                    factory.getMaxPipeLevel(),
                    factory.getDescription(),
                    buckets,
                    busLines));
        }

        for (ExternalConnection connection : externalConnectionRepository.findAll()) {
            String sourceRef = busLineRefById.get(connection.getSourceBusLine().getId());
            String targetRef = busLineRefById.get(connection.getTargetBusLine().getId());
            if (sourceRef == null || targetRef == null) {
                continue;
            }
            connections.add(new ExternalConnectionExport(sourceRef, targetRef, connection.isEnabled()));
        }

        return new SaveFile(SAVE_VERSION, Instant.now().toString(), factories, connections);
    }

    public MessageResponse importSave(SaveFile saveFile, boolean overwrite) {
        validate(saveFile);

        if (overwrite) {
            factoryRepository.deleteAll();
            factoryRepository.flush();
        }

        Map<String, Factory> factoryMap = new LinkedHashMap<>();
        Map<String, BusLine> busLineMap = new LinkedHashMap<>();

        for (FactoryExport item : saveFile.factories()) {
            Factory factory = new Factory();
            factory.setName(item.name());
            factory.setFactoryType(parseFactoryType(item.factoryType()));
            factory.setEnabled(Boolean.TRUE.equals(item.enabled()));
            factory.setMaxBeltLevel(item.maxBeltLevel() == null ? 3 : item.maxBeltLevel());
            factory.setMaxPipeLevel(item.maxPipeLevel() == null ? 1 : item.maxPipeLevel());
            factory.setDescription(item.description());
            Factory savedFactory = factoryRepository.save(factory);
            factoryMap.put(item.ref(), savedFactory);

            for (ProductionBucketExport bucketItem : item.buckets()) {
                ProductionBucket bucket = new ProductionBucket();
                bucket.setFactory(savedFactory);
                bucket.setName(bucketItem.name());
                bucket.setEnabled(Boolean.TRUE.equals(bucketItem.enabled()));
                bucket.setDescription(bucketItem.description());
                bucket.setPositionX(bucketItem.positionX());
                bucket.setPositionY(bucketItem.positionY());
                bucket.setCollapsed(Boolean.TRUE.equals(bucketItem.collapsed()));
                bucket.setSortOrder(bucketItem.sortOrder() == null ? 0 : bucketItem.sortOrder());
                ProductionBucket savedBucket = bucketRepository.save(bucket);

                for (ProductionNodeExport nodeItem : bucketItem.nodes()) {
                    Recipe recipe = recipeRepository.findByGameKey(nodeItem.recipeGameKey())
                            .orElseThrow(() -> new BadRequestException("Recipe not found by gameKey: " + nodeItem.recipeGameKey()));
                    ProductionNode node = new ProductionNode();
                    node.setBucket(savedBucket);
                    node.setRecipe(recipe);
                    node.setEnabled(Boolean.TRUE.equals(nodeItem.enabled()));
                    node.setMachineCount(nodeItem.machineCount() == null ? 1.0 : nodeItem.machineCount());
                    node.setClockPercent(nodeItem.clockPercent() == null ? 100.0 : nodeItem.clockPercent());
                    node.setOutputMultiplier(nodeItem.outputMultiplier() == null ? 1.0 : nodeItem.outputMultiplier());
                    node.setName(nodeItem.name());
                    node.setPositionX(nodeItem.positionX());
                    node.setPositionY(nodeItem.positionY());
                    node.setSortOrder(nodeItem.sortOrder() == null ? 0 : nodeItem.sortOrder());
                    nodeRepository.save(node);
                }
            }

            for (BusLineExport lineItem : item.busLines()) {
                Material material = materialRepository.findByGameKey(lineItem.materialGameKey())
                        .orElseThrow(() -> new BadRequestException("Material not found by gameKey: " + lineItem.materialGameKey()));
                BusLine line = new BusLine();
                line.setFactory(savedFactory);
                line.setMaterial(material);
                line.setName(lineItem.name());
                line.setDescription(lineItem.description());
                line.setOffsetAmount(lineItem.offsetAmount() == null ? 0.0 : lineItem.offsetAmount());
                line.setVisibleToOtherFactories(Boolean.TRUE.equals(lineItem.visibleToOtherFactories()));
                line.setExternalEnabled(Boolean.TRUE.equals(lineItem.externalEnabled()));
                line.setSortOrder(lineItem.sortOrder() == null ? 0 : lineItem.sortOrder());
                line.setCollapsed(Boolean.TRUE.equals(lineItem.collapsed()));
                line.setCreatedManually(Boolean.TRUE.equals(lineItem.createdManually()));
                BusLine savedLine = busLineRepository.save(line);
                busLineMap.put(lineItem.ref(), savedLine);
            }
        }

        for (ExternalConnectionExport connectionItem : saveFile.externalConnections()) {
            BusLine source = busLineMap.get(connectionItem.sourceBusLineRef());
            BusLine target = busLineMap.get(connectionItem.targetBusLineRef());
            if (source == null || target == null) {
                throw new BadRequestException("External connection references unknown bus line ref.");
            }
            ExternalConnection connection = new ExternalConnection();
            connection.setSourceBusLine(source);
            connection.setTargetBusLine(target);
            connection.setEnabled(connectionItem.enabled() == null || connectionItem.enabled());
            externalConnectionRepository.save(connection);
        }

        return new MessageResponse("Save imported: factories=" + saveFile.factories().size() + ", externalConnections=" + saveFile.externalConnections().size() + ".");
    }

    @Transactional(readOnly = true)
    public void writeSaveToFile(Path outputPath) {
        try {
            File file = outputPath.toFile();
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, exportSave());
        } catch (IOException e) {
            throw new BadRequestException("Failed to write save file: " + e.getMessage());
        }
    }

    public void importFromFile(Path inputPath, boolean overwrite) {
        try {
            SaveFile saveFile = objectMapper.readValue(inputPath.toFile(), SaveFile.class);
            importSave(saveFile, overwrite);
        } catch (IOException e) {
            throw new BadRequestException("Failed to read save file: " + e.getMessage());
        }
    }

    private com.example.satisfactory.enums.FactoryType parseFactoryType(String value) {
        if (value == null || value.isBlank()) {
            return com.example.satisfactory.enums.FactoryType.SUB;
        }
        try {
            return com.example.satisfactory.enums.FactoryType.valueOf(value.trim().toUpperCase());
        } catch (Exception e) {
            throw new BadRequestException("Invalid factoryType in save file: " + value);
        }
    }

    private void validate(SaveFile saveFile) {
        if (saveFile == null) {
            throw new BadRequestException("Save file is empty.");
        }
        if (saveFile.version() == null || saveFile.version() < 1) {
            throw new BadRequestException("Unsupported save file version.");
        }
        if (saveFile.factories() == null) {
            throw new BadRequestException("Save file missing factories.");
        }
        for (FactoryExport factory : saveFile.factories()) {
            if (factory.ref() == null || factory.ref().isBlank()) {
                throw new BadRequestException("Factory ref is required.");
            }
            if (factory.name() == null || factory.name().isBlank()) {
                throw new BadRequestException("Factory name is required.");
            }
        }
    }

    public record SaveFile(Integer version,
                           String exportedAt,
                           List<FactoryExport> factories,
                           List<ExternalConnectionExport> externalConnections) {
        public SaveFile {
            factories = factories == null ? List.of() : factories;
            externalConnections = externalConnections == null ? List.of() : externalConnections;
        }
    }

    public record FactoryExport(String ref,
                                String name,
                                String factoryType,
                                Boolean enabled,
                                Integer maxBeltLevel,
                                Integer maxPipeLevel,
                                String description,
                                List<ProductionBucketExport> buckets,
                                List<BusLineExport> busLines) {
        public FactoryExport {
            buckets = buckets == null ? List.of() : buckets;
            busLines = busLines == null ? List.of() : busLines;
        }
    }

    public record ProductionBucketExport(String name,
                                         Boolean enabled,
                                         String description,
                                         Double positionX,
                                         Double positionY,
                                         Boolean collapsed,
                                         Integer sortOrder,
                                         List<ProductionNodeExport> nodes) {
        public ProductionBucketExport {
            nodes = nodes == null ? List.of() : nodes;
        }
    }

    public record ProductionNodeExport(String recipeGameKey,
                                       Boolean enabled,
                                       Double machineCount,
                                       Double clockPercent,
                                       Double outputMultiplier,
                                       String name,
                                       Double positionX,
                                       Double positionY,
                                       Integer sortOrder) {}

    public record BusLineExport(String ref,
                                String materialGameKey,
                                String name,
                                String description,
                                Double offsetAmount,
                                Boolean visibleToOtherFactories,
                                Boolean externalEnabled,
                                Integer sortOrder,
                                Boolean collapsed,
                                Boolean createdManually) {
        public BusLineExport {
            if (ref == null || ref.isBlank()) {
                ref = "bus-" + UUID.randomUUID();
            }
        }
    }

    public record ExternalConnectionExport(String sourceBusLineRef,
                                           String targetBusLineRef,
                                           Boolean enabled) {}
}
