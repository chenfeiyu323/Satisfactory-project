package com.example.satisfactory.dto;

import com.example.satisfactory.enums.MachineType;

public record MachineDto(Long id, String gameKey, String name, MachineType machineType, Double powerMw, boolean enabled) {}
