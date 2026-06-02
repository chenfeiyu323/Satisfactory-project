package com.example.satisfactory.dto;

public record RecipeMaterialAmountDto(Long materialId, String materialName, String materialGameKey, Double amountPerCycle, Double amountPerMinuteAt100Percent) {}
