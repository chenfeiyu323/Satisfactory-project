package com.example.satisfactory.dto;

import com.example.satisfactory.enums.FactoryType;
import java.time.Instant;

public record FactoryResponse(
        Long id,
        String name,
        FactoryType factoryType,
        boolean enabled,
        Integer maxBeltLevel,
        Integer maxPipeLevel,
        String description,
        Instant createdAt,
        Instant updatedAt
) {}
