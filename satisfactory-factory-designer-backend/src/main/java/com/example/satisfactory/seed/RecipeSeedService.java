package com.example.satisfactory.seed;

import com.example.satisfactory.dto.MessageResponse;
import com.example.satisfactory.entity.*;
import com.example.satisfactory.enums.MachineType;
import com.example.satisfactory.enums.MaterialType;
import com.example.satisfactory.enums.TransportType;
import com.example.satisfactory.exception.BadRequestException;
import com.example.satisfactory.repository.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.List;

@Service
@Transactional
public class RecipeSeedService {
    private final ObjectMapper objectMapper;
    private final MaterialRepository materialRepository;
    private final MachineRepository machineRepository;
    private final RecipeRepository recipeRepository;
    private final TransportLevelRepository transportLevelRepository;

    public RecipeSeedService(ObjectMapper objectMapper, MaterialRepository materialRepository, MachineRepository machineRepository, RecipeRepository recipeRepository, TransportLevelRepository transportLevelRepository) {
        this.objectMapper = objectMapper;
        this.materialRepository = materialRepository;
        this.machineRepository = machineRepository;
        this.recipeRepository = recipeRepository;
        this.transportLevelRepository = transportLevelRepository;
    }

    public MessageResponse seedAll() {
        int transport = seedTransportLevels("data/seed/transport_levels.json");
        int materials = seedMaterials("data/seed/materials.json");
        int machines = seedMachines("data/seed/machines.json");
        int recipes = seedRecipes("data/seed/recipes.json");
        return new MessageResponse("Seed completed: transport=" + transport + ", materials=" + materials + ", machines=" + machines + ", recipes=" + recipes + ".");
    }

    public int seedTransportLevels(String path) {
        List<TransportSeed> seeds = read(path, new TypeReference<>() {});
        for (TransportSeed seed : seeds) {
            TransportLevel level = transportLevelRepository.findByTransportTypeAndLevel(seed.transportType(), seed.level()).orElseGet(TransportLevel::new);
            level.setTransportType(seed.transportType());
            level.setLevel(seed.level());
            level.setName(seed.name());
            level.setCapacityPerMin(seed.capacityPerMin());
            level.setSortOrder(seed.sortOrder() == null ? seed.level() : seed.sortOrder());
            transportLevelRepository.save(level);
        }
        return seeds.size();
    }

    public int seedMaterials(String path) {
        List<MaterialSeed> seeds = read(path, new TypeReference<>() {});
        for (MaterialSeed seed : seeds) {
            Material material = materialRepository.findByGameKey(seed.gameKey()).orElseGet(Material::new);
            material.setGameKey(seed.gameKey());
            material.setName(seed.name());
            material.setMaterialType(seed.materialType());
            material.setStackSize(seed.stackSize());
            material.setSinkable(seed.sinkable() == null || seed.sinkable());
            material.setEnabled(seed.enabled() == null || seed.enabled());
            material.setDescription(seed.description());
            materialRepository.save(material);
        }
        return seeds.size();
    }

    public int seedMachines(String path) {
        List<MachineSeed> seeds = read(path, new TypeReference<>() {});
        for (MachineSeed seed : seeds) {
            Machine machine = machineRepository.findByGameKey(seed.gameKey()).orElseGet(Machine::new);
            machine.setGameKey(seed.gameKey());
            machine.setName(seed.name());
            machine.setMachineType(seed.machineType());
            machine.setPowerMw(seed.powerMw());
            machine.setEnabled(seed.enabled() == null || seed.enabled());
            machineRepository.save(machine);
        }
        return seeds.size();
    }

    public int seedRecipes(String path) {
        List<RecipeSeed> seeds = read(path, new TypeReference<>() {});
        for (RecipeSeed seed : seeds) {
            Machine machine = machineRepository.findByGameKey(seed.machineKey())
                    .orElseThrow(() -> new BadRequestException("Machine not found for recipe " + seed.gameKey() + ": " + seed.machineKey()));
            Recipe recipe = recipeRepository.findByGameKey(seed.gameKey()).orElseGet(Recipe::new);
            recipe.setGameKey(seed.gameKey());
            recipe.setName(seed.name());
            recipe.setMachine(machine);
            recipe.setCycleTimeSeconds(seed.cycleTimeSeconds());
            recipe.setAlternate(seed.alternate() != null && seed.alternate());
            recipe.setSource(seed.source() == null ? "OFFICIAL" : seed.source());
            recipe.setGameVersion(seed.gameVersion());
            recipe.setEnabled(seed.enabled() == null || seed.enabled());
            recipe.getInputs().clear();
            recipe.getOutputs().clear();
            for (MaterialAmountSeed inputSeed : seed.inputs()) {
                Material material = materialRepository.findByGameKey(inputSeed.materialKey())
                        .orElseThrow(() -> new BadRequestException("Input material not found for recipe " + seed.gameKey() + ": " + inputSeed.materialKey()));
                RecipeInput input = new RecipeInput();
                input.setRecipe(recipe);
                input.setMaterial(material);
                input.setAmountPerCycle(inputSeed.amountPerCycle());
                recipe.getInputs().add(input);
            }
            for (MaterialAmountSeed outputSeed : seed.outputs()) {
                Material material = materialRepository.findByGameKey(outputSeed.materialKey())
                        .orElseThrow(() -> new BadRequestException("Output material not found for recipe " + seed.gameKey() + ": " + outputSeed.materialKey()));
                RecipeOutput output = new RecipeOutput();
                output.setRecipe(recipe);
                output.setMaterial(material);
                output.setAmountPerCycle(outputSeed.amountPerCycle());
                recipe.getOutputs().add(output);
            }
            recipeRepository.save(recipe);
        }
        return seeds.size();
    }

    private <T> T read(String path, TypeReference<T> typeReference) {
        try (InputStream in = new ClassPathResource(path).getInputStream()) {
            return objectMapper.readValue(in, typeReference);
        } catch (Exception e) {
            throw new BadRequestException("Failed to read seed file " + path + ": " + e.getMessage());
        }
    }

    public record MaterialSeed(String gameKey, String name, MaterialType materialType, Integer stackSize, Boolean sinkable, Boolean enabled, String description) {}
    public record MachineSeed(String gameKey, String name, MachineType machineType, Double powerMw, Boolean enabled) {}
    public record TransportSeed(TransportType transportType, Integer level, String name, Double capacityPerMin, Integer sortOrder) {}
    public record MaterialAmountSeed(String materialKey, Double amountPerCycle) {}
    public record RecipeSeed(String gameKey, String name, String machineKey, Double cycleTimeSeconds, Boolean alternate, String source, String gameVersion, Boolean enabled, List<MaterialAmountSeed> inputs, List<MaterialAmountSeed> outputs) {}
}
