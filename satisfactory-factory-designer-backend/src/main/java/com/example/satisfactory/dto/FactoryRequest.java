package com.example.satisfactory.dto;

import com.example.satisfactory.enums.FactoryType;
import jakarta.validation.constraints.NotBlank;

public record FactoryRequest(
        @NotBlank String name,
        FactoryType factoryType,
        Boolean enabled,
        Integer maxBeltLevel,
        Integer maxPipeLevel,
        String description
) {}
