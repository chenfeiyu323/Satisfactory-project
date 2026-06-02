package com.example.satisfactory.dto;

import jakarta.validation.constraints.NotBlank;

public record ProductionBucketRequest(
        @NotBlank String name,
        Boolean enabled,
        String description,
        Double positionX,
        Double positionY,
        Boolean collapsed,
        Integer sortOrder
) {}
