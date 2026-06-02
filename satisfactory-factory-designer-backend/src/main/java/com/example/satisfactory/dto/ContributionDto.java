package com.example.satisfactory.dto;

public record ContributionDto(
        Long sourceId,
        String sourceName,
        Double amount,
        String type
) {}
