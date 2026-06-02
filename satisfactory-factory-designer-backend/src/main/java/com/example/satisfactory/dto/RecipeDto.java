package com.example.satisfactory.dto;

import java.util.List;

public record RecipeDto(
        Long id,
        String gameKey,
        String name,
        Long machineId,
        String machineName,
        Double cycleTimeSeconds,
        boolean alternate,
        String source,
        String gameVersion,
        boolean enabled,
        List<RecipeMaterialAmountDto> inputs,
        List<RecipeMaterialAmountDto> outputs
) {}
