package com.example.satisfactory.dto;

import jakarta.validation.constraints.NotBlank;

public record SnapshotRequest(@NotBlank String name) {}
