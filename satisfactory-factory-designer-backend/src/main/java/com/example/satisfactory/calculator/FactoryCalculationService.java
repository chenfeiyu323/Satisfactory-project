package com.example.satisfactory.calculator;

import com.example.satisfactory.dto.*;
import com.example.satisfactory.entity.*;
import com.example.satisfactory.enums.HealthStatus;
import com.example.satisfactory.enums.MaterialType;
import com.example.satisfactory.enums.TransportType;
import com.example.satisfactory.exception.NotFoundException;
import com.example.satisfactory.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional(readOnly = true)
public class FactoryCalculationService {
    private final FactoryRepository factoryRepository;
    private final BusLineRepository busLineRepository;
    private final ProductionNodeRepository nodeRepository;
    private final ExternalConnectionRepository connectionRepository;
    private final TransportLevelRepository transportLevelRepository;

    public FactoryCalculationService(
            FactoryRepository factoryRepository,
            BusLineRepository busLineRepository,
            ProductionNodeRepository nodeRepository,
            ExternalConnectionRepository connectionRepository,
            TransportLevelRepository transportLevelRepository
    ) {
        this.factoryRepository = factoryRepository;
        this.busLineRepository = busLineRepository;
        this.nodeRepository = nodeRepository;
        this.connectionRepository = connectionRepository;
        this.transportLevelRepository = transportLevelRepository;
    }

    @Transactional
    public FactoryCalculationDto calculateFactory(Long factoryId) {
        Factory factory = factoryRepository.findById(factoryId).orElseThrow(() -> new NotFoundException("Factory not found: " + factoryId));
        List<String> warnings = new ArrayList<>();
        if (!factory.isEnabled()) {
            warnings.add("Factory is disabled; calculation is shown for editing reference only and will not provide external output.");
        }

        FactoryLocalTotals totals = calculateLocalTotals(factoryId);
        List<BusLine> lines = busLineRepository.findByFactoryIdOrderBySortOrderAscIdAsc(factoryId);
        if (factory.isEnabled()) {
            Set<Long> materialIdsInLines = new HashSet<>();
            for (BusLine line : lines) materialIdsInLines.add(line.getMaterial().getId());
            for (Map.Entry<Long, Material> entry : totals.materials.entrySet()) {
                if (!materialIdsInLines.contains(entry.getKey())) {
                    BusLine line = new BusLine();
                    line.setFactory(factory);
                    line.setMaterial(entry.getValue());
                    line.setName(entry.getValue().getName());
                    line.setOffsetAmount(0.0);
                    line.setCreatedManually(false);
                    busLineRepository.save(line);
                }
            }
            lines = busLineRepository.findByFactoryIdOrderBySortOrderAscIdAsc(factoryId);
        }

        List<BusLineCalculationDto> resultLines = new ArrayList<>();
        for (BusLine line : lines) {
            resultLines.add(calculateBusLine(factory, line, totals, new HashSet<>()));
        }

        HealthStatus overall = HealthStatus.GREEN;
        for (BusLineCalculationDto line : resultLines) {
            if (line.status() == HealthStatus.RED) overall = HealthStatus.RED;
            else if (line.status() == HealthStatus.YELLOW && overall != HealthStatus.RED) overall = HealthStatus.YELLOW;
        }
        if (!factory.isEnabled() && overall == HealthStatus.GREEN) overall = HealthStatus.GRAY;

        return new FactoryCalculationDto(factory.getId(), factory.getName(), factory.isEnabled(), overall, warnings, resultLines);
    }

    public double calculateBusLineNet(Long busLineId) {
        BusLine line = busLineRepository.findById(busLineId).orElseThrow(() -> new NotFoundException("Bus line not found: " + busLineId));
        return calculateBusLineNet(line, new HashSet<>());
    }

    private BusLineCalculationDto calculateBusLine(Factory factory, BusLine line, FactoryLocalTotals totals, Set<Long> visiting) {
        Long materialId = line.getMaterial().getId();
        double localOutput = totals.outputs.getOrDefault(materialId, 0.0);
        double localDemand = totals.inputs.getOrDefault(materialId, 0.0);
        double externalInput = 0.0;
        List<ContributionDto> externalSources = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        for (ExternalConnection connection : connectionRepository.findByTargetBusLineIdAndEnabledTrue(line.getId())) {
            BusLine source = connection.getSourceBusLine();
            if (!source.getFactory().isEnabled()) {
                warnings.add("External source factory is disabled: " + source.getFactory().getName());
                continue;
            }
            double sourceNet = Math.max(0.0, calculateBusLineNet(source, new HashSet<>(visiting)));
            externalInput += sourceNet;
            externalSources.add(new ContributionDto(source.getId(), source.getFactory().getName() + " / " + source.getName(), sourceNet, "EXTERNAL_INPUT"));
        }

        double offset = safe(line.getOffsetAmount());
        double net = localOutput + externalInput + offset - localDemand;
        TransportAdviceDto advice = buildTransportAdvice(factory, line.getMaterial(), localOutput, localDemand, externalInput, offset);

        HealthStatus status;
        if (!factory.isEnabled()) {
            status = HealthStatus.GRAY;
        } else if (net < -0.000001 || advice.overCurrentMax()) {
            status = HealthStatus.RED;
        } else if (!warnings.isEmpty() || (externalInput > 0 && externalInput > advice.requiredThroughput() + 0.000001)) {
            status = HealthStatus.YELLOW;
        } else {
            status = HealthStatus.GREEN;
        }

        if (net < 0) warnings.add("Net value is negative; this line cannot be used as an external output source.");
        if (externalInput > 0 && externalInput > advice.requiredThroughput() + 0.000001) {
            warnings.add("External input is higher than the active line requirement; frontend can mark the external input number yellow.");
        }
        if (advice.overCurrentMax()) warnings.add(advice.message());

        boolean connectedAsSource = connectionRepository.existsBySourceBusLineId(line.getId());
        return new BusLineCalculationDto(
                line.getId(), materialId, line.getMaterial().getName(), line.getMaterial().getMaterialType(), line.getName(),
                round(localOutput), round(localDemand), round(externalInput), round(offset), round(net), status, warnings,
                totals.producers.getOrDefault(materialId, List.of()),
                totals.consumers.getOrDefault(materialId, List.of()),
                externalSources,
                advice,
                line.isExternalEnabled(),
                line.isVisibleToOtherFactories(),
                connectedAsSource
        );
    }

    private double calculateBusLineNet(BusLine line, Set<Long> visiting) {
        if (!line.getFactory().isEnabled()) return 0.0;
        if (!visiting.add(line.getId())) return 0.0;
        FactoryLocalTotals totals = calculateLocalTotals(line.getFactory().getId());
        Long materialId = line.getMaterial().getId();
        double localOutput = totals.outputs.getOrDefault(materialId, 0.0);
        double localDemand = totals.inputs.getOrDefault(materialId, 0.0);
        double externalInput = 0.0;
        for (ExternalConnection connection : connectionRepository.findByTargetBusLineIdAndEnabledTrue(line.getId())) {
            BusLine source = connection.getSourceBusLine();
            externalInput += Math.max(0.0, calculateBusLineNet(source, new HashSet<>(visiting)));
        }
        return localOutput + externalInput + safe(line.getOffsetAmount()) - localDemand;
    }

    private FactoryLocalTotals calculateLocalTotals(Long factoryId) {
        FactoryLocalTotals totals = new FactoryLocalTotals();
        List<ProductionNode> nodes = nodeRepository.findByBucketFactoryIdAndBucketEnabledTrueAndEnabledTrue(factoryId);
        for (ProductionNode node : nodes) {
            Recipe recipe = node.getRecipe();
            double machineFactor = safe(node.getMachineCount()) * safe(node.getClockPercent()) / 100.0;
            double inputFactor = machineFactor;
            double outputFactor = machineFactor * safe(node.getOutputMultiplier());
            String nodeName = node.getName() == null || node.getName().isBlank() ? recipe.getName() : node.getName();
            for (RecipeInput input : recipe.getInputs()) {
                double amount = input.getAmountPerCycle() * 60.0 / recipe.getCycleTimeSeconds() * inputFactor;
                totals.materials.put(input.getMaterial().getId(), input.getMaterial());
                add(totals.inputs, input.getMaterial().getId(), amount);
                addContribution(totals.consumers, input.getMaterial().getId(), new ContributionDto(node.getId(), nodeName, round(amount), "CONSUMER"));
            }
            for (RecipeOutput output : recipe.getOutputs()) {
                double amount = output.getAmountPerCycle() * 60.0 / recipe.getCycleTimeSeconds() * outputFactor;
                totals.materials.put(output.getMaterial().getId(), output.getMaterial());
                add(totals.outputs, output.getMaterial().getId(), amount);
                addContribution(totals.producers, output.getMaterial().getId(), new ContributionDto(node.getId(), nodeName, round(amount), "PRODUCER"));
            }
        }
        return totals;
    }

    private TransportAdviceDto buildTransportAdvice(Factory factory, Material material, double localOutput, double localDemand, double externalInput, double offset) {
        TransportType type = material.getMaterialType() == MaterialType.SOLID ? TransportType.BELT : TransportType.PIPE;
        int currentMaxLevel = type == TransportType.BELT ? factory.getMaxBeltLevel() : factory.getMaxPipeLevel();
        double positiveOffset = Math.max(0.0, offset);
        double requiredThroughput = Math.max(localDemand, localOutput + positiveOffset);
        List<TransportLevel> levels = transportLevelRepository.findByTransportTypeOrderBySortOrderAsc(type);
        TransportLevel current = levels.stream().filter(l -> Objects.equals(l.getLevel(), currentMaxLevel)).findFirst().orElse(null);
        double currentCapacity = current == null ? 0.0 : current.getCapacityPerMin();
        TransportLevel recommended = levels.stream()
                .filter(l -> l.getCapacityPerMin() + 0.000001 >= requiredThroughput)
                .findFirst()
                .orElse(null);

        boolean overCurrent = currentCapacity > 0 && requiredThroughput > currentCapacity + 0.000001;
        String message;
        Integer recommendedLevel = recommended == null ? null : recommended.getLevel();
        String recommendedName = recommended == null ? null : recommended.getName();
        if (requiredThroughput <= 0.000001) {
            message = "No active throughput requirement.";
        } else if (recommended == null) {
            message = "Required throughput exceeds all configured " + type + " levels; split the line or add higher tech data.";
            overCurrent = true;
        } else if (overCurrent) {
            message = "Recommended upgrade to " + recommended.getName() + ". Current max level cannot carry this line.";
        } else {
            message = (current == null ? recommended.getName() : current.getName()) + " is enough.";
        }
        return new TransportAdviceDto(type, round(requiredThroughput), currentMaxLevel, round(currentCapacity), recommendedLevel, recommendedName, message, overCurrent);
    }

    private void add(Map<Long, Double> map, Long key, double amount) {
        map.merge(key, amount, Double::sum);
    }

    private void addContribution(Map<Long, List<ContributionDto>> map, Long key, ContributionDto contribution) {
        map.computeIfAbsent(key, ignored -> new ArrayList<>()).add(contribution);
    }

    private double safe(Double value) {
        return value == null ? 0.0 : value;
    }

    private double round(double value) {
        return Math.round(value * 10000.0) / 10000.0;
    }

    private static class FactoryLocalTotals {
        Map<Long, Double> inputs = new HashMap<>();
        Map<Long, Double> outputs = new HashMap<>();
        Map<Long, List<ContributionDto>> producers = new HashMap<>();
        Map<Long, List<ContributionDto>> consumers = new HashMap<>();
        Map<Long, Material> materials = new HashMap<>();
    }
}
