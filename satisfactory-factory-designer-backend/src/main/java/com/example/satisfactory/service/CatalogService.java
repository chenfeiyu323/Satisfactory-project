package com.example.satisfactory.service;

import com.example.satisfactory.dto.*;
import com.example.satisfactory.entity.*;
import com.example.satisfactory.repository.MachineRepository;
import com.example.satisfactory.repository.MaterialRepository;
import com.example.satisfactory.repository.RecipeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class CatalogService {
    private final MaterialRepository materialRepository;
    private final MachineRepository machineRepository;
    private final RecipeRepository recipeRepository;

    public CatalogService(MaterialRepository materialRepository, MachineRepository machineRepository, RecipeRepository recipeRepository) {
        this.materialRepository = materialRepository;
        this.machineRepository = machineRepository;
        this.recipeRepository = recipeRepository;
    }

    public List<MaterialDto> getMaterials() {
        return materialRepository.findAll().stream()
                .sorted(Comparator.comparing(Material::getName))
                .map(this::toMaterialDto)
                .toList();
    }

    public List<MachineDto> getMachines() {
        return machineRepository.findAll().stream()
                .sorted(Comparator.comparing(Machine::getName))
                .map(this::toMachineDto)
                .toList();
    }

    public List<RecipeDto> getRecipes() {
        return recipeRepository.findAll().stream()
                .sorted(Comparator.comparing(Recipe::getName))
                .map(this::toRecipeDto)
                .toList();
    }

    public MaterialDto toMaterialDto(Material material) {
        return new MaterialDto(material.getId(), material.getGameKey(), material.getName(), material.getMaterialType(), material.getStackSize(), material.isSinkable(), material.isEnabled(), material.getDescription());
    }

    public MachineDto toMachineDto(Machine machine) {
        return new MachineDto(machine.getId(), machine.getGameKey(), machine.getName(), machine.getMachineType(), machine.getPowerMw(), machine.isEnabled());
    }

    public RecipeDto toRecipeDto(Recipe recipe) {
        List<RecipeMaterialAmountDto> inputs = recipe.getInputs().stream()
                .map(input -> new RecipeMaterialAmountDto(
                        input.getMaterial().getId(), input.getMaterial().getName(), input.getMaterial().getGameKey(), input.getAmountPerCycle(), perMinute(input.getAmountPerCycle(), recipe.getCycleTimeSeconds())
                ))
                .toList();
        List<RecipeMaterialAmountDto> outputs = recipe.getOutputs().stream()
                .map(output -> new RecipeMaterialAmountDto(
                        output.getMaterial().getId(), output.getMaterial().getName(), output.getMaterial().getGameKey(), output.getAmountPerCycle(), perMinute(output.getAmountPerCycle(), recipe.getCycleTimeSeconds())
                ))
                .toList();
        return new RecipeDto(recipe.getId(), recipe.getGameKey(), recipe.getName(), recipe.getMachine().getId(), recipe.getMachine().getName(), recipe.getCycleTimeSeconds(), recipe.isAlternate(), recipe.getSource(), recipe.getGameVersion(), recipe.isEnabled(), inputs, outputs);
    }

    private Double perMinute(Double amountPerCycle, Double cycleSeconds) {
        return amountPerCycle * 60.0 / cycleSeconds;
    }
}
