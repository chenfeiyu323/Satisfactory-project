package com.example.satisfactory.dto;

import com.example.satisfactory.enums.MaterialType;

public record MaterialDto(Long id, String gameKey, String name, MaterialType materialType, Integer stackSize, boolean sinkable, boolean enabled, String description) {}
