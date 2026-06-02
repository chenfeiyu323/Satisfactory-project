package com.example.satisfactory.dto;

import com.example.satisfactory.enums.HealthStatus;
import java.util.List;

public record FactoryCalculationDto(
        Long factoryId,
        String factoryName,
        boolean enabled,
        HealthStatus overallStatus,
        List<String> warnings,
        List<BusLineCalculationDto> busLines
) {}
