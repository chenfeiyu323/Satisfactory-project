package com.example.satisfactory.dto;

import com.example.satisfactory.enums.HealthStatus;
import com.example.satisfactory.enums.MaterialType;
import java.util.List;

public record BusLineCalculationDto(
        Long busLineId,
        Long materialId,
        String materialName,
        MaterialType materialType,
        String lineName,
        Double localOutput,
        Double localDemand,
        Double externalInput,
        Double offset,
        Double net,
        HealthStatus status,
        List<String> warnings,
        List<ContributionDto> producers,
        List<ContributionDto> consumers,
        List<ContributionDto> externalSources,
        TransportAdviceDto transportAdvice,
        boolean externalEnabled,
        boolean visibleToOtherFactories,
        boolean connectedAsSource
) {}
