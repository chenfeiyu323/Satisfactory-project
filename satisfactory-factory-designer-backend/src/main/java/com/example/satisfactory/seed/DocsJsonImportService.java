package com.example.satisfactory.seed;

import com.example.satisfactory.dto.MessageResponse;
import com.example.satisfactory.entity.*;
import com.example.satisfactory.enums.MachineType;
import com.example.satisfactory.enums.MaterialType;
import com.example.satisfactory.exception.BadRequestException;
import com.example.satisfactory.repository.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Transactional
public class DocsJsonImportService {
    private static final Pattern ITEM_AMOUNT_PATTERN = Pattern.compile("ItemClass=\\\"[^\\\"]*?\\.([^.'\\\"]+)'\\\",Amount=([0-9.Ee+\\-]+)");

    private static final Map<String, Integer> STACK_SIZE_MAP = Map.of(
            "SS_ONE", 1,
            "SS_SMALL", 50,
            "SS_MEDIUM", 100,
            "SS_BIG", 200,
            "SS_HUGE", 500,
            "SS_FLUID", 0
    );

    private static final Map<String, MaterialType> MATERIAL_FORM_MAP = Map.of(
            "RF_SOLID", MaterialType.SOLID,
            "RF_LIQUID", MaterialType.FLUID,
            "RF_GAS", MaterialType.GAS,
            "RF_INVALID", MaterialType.SOLID
    );

    private record MachineSeed(String key, String name, MachineType type, double powerMw) {}

    private static final List<MachineSeed> BASE_MACHINES = List.of(
            new MachineSeed("miner", "Miner", MachineType.MINER, 5),
            new MachineSeed("water_extractor", "Water Extractor", MachineType.WATER_EXTRACTOR, 20),
            new MachineSeed("oil_extractor", "Oil Extractor", MachineType.OTHER, 40),
            new MachineSeed("resource_well_extractor", "Resource Well Extractor", MachineType.OTHER, 0),
            new MachineSeed("smelter", "Smelter", MachineType.SMELTER, 4),
            new MachineSeed("foundry", "Foundry", MachineType.FOUNDRY, 16),
            new MachineSeed("constructor", "Constructor", MachineType.CONSTRUCTOR, 4),
            new MachineSeed("assembler", "Assembler", MachineType.ASSEMBLER, 15),
            new MachineSeed("manufacturer", "Manufacturer", MachineType.MANUFACTURER, 55),
            new MachineSeed("refinery", "Refinery", MachineType.REFINERY, 30),
            new MachineSeed("packager", "Packager", MachineType.PACKAGER, 10),
            new MachineSeed("blender", "Blender", MachineType.BLENDER, 75),
            new MachineSeed("particle_accelerator", "Particle Accelerator", MachineType.PARTICLE_ACCELERATOR, 250),
            new MachineSeed("converter", "Converter", MachineType.OTHER, 250),
            new MachineSeed("quantum_encoder", "Quantum Encoder", MachineType.OTHER, 1000),
            new MachineSeed("other", "Other", MachineType.OTHER, 0)
    );

    private static final List<Map.Entry<String, String>> PRODUCER_TO_MACHINE = List.of(
            Map.entry("Build_Smelter", "smelter"),
            Map.entry("Build_Foundry", "foundry"),
            Map.entry("Build_Constructor", "constructor"),
            Map.entry("Build_Assembler", "assembler"),
            Map.entry("Build_Manufacturer", "manufacturer"),
            Map.entry("Build_OilRefinery", "refinery"),
            Map.entry("Build_Refinery", "refinery"),
            Map.entry("Build_Packager", "packager"),
            Map.entry("Build_Blender", "blender"),
            Map.entry("Build_HadronCollider", "particle_accelerator"),
            Map.entry("Build_ParticleAccelerator", "particle_accelerator"),
            Map.entry("Build_Converter", "converter"),
            Map.entry("Build_QuantumEncoder", "quantum_encoder"),
            Map.entry("Build_FrackingExtractor", "resource_well_extractor"),
            Map.entry("Build_OilPump", "oil_extractor"),
            Map.entry("Build_Miner", "miner"),
            Map.entry("Build_WaterPump", "water_extractor")
    );

    private final ObjectMapper objectMapper;
    private final MaterialRepository materialRepository;
    private final MachineRepository machineRepository;
    private final RecipeRepository recipeRepository;

    public DocsJsonImportService(ObjectMapper objectMapper,
                                 MaterialRepository materialRepository,
                                 MachineRepository machineRepository,
                                 RecipeRepository recipeRepository) {
        this.objectMapper = objectMapper;
        this.materialRepository = materialRepository;
        this.machineRepository = machineRepository;
        this.recipeRepository = recipeRepository;
    }

    public MessageResponse importDocsJson(MultipartFile file, String gameVersion) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Docs.json file is required.");
        }
        try (InputStream in = file.getInputStream()) {
            JsonNode root = objectMapper.readTree(in);
            if (!root.isArray()) {
                throw new BadRequestException("Docs.json root must be an array.");
            }

            Map<String, String> classToMaterialKey = new HashMap<>();
            Map<String, MaterialType> materialTypes = new HashMap<>();

            int machineCount = seedBaseMachines();
            int materialCount = importMaterials(root, classToMaterialKey, materialTypes);
            int recipeCount = importRecipes(root, classToMaterialKey, materialTypes, gameVersion == null || gameVersion.isBlank() ? "Docs.json" : gameVersion.trim());

            return new MessageResponse("Docs.json import completed: materials=" + materialCount + ", machines=" + machineCount + ", recipes=" + recipeCount + ". Refresh the frontend after import.");
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new BadRequestException("Failed to import Docs.json: " + e.getMessage());
        }
    }

    private int seedBaseMachines() {
        for (MachineSeed seed : BASE_MACHINES) {
            Machine machine = machineRepository.findByGameKey(seed.key()).orElseGet(Machine::new);
            machine.setGameKey(seed.key());
            machine.setName(seed.name());
            machine.setMachineType(seed.type());
            machine.setPowerMw(seed.powerMw());
            machine.setEnabled(true);
            machineRepository.save(machine);
        }
        return BASE_MACHINES.size();
    }

    private int importMaterials(JsonNode docs, Map<String, String> classToMaterialKey, Map<String, MaterialType> materialTypes) {
        int count = 0;
        Set<String> seenKeys = new HashSet<>();
        for (JsonNode item : findClasses(docs, List.of("FGItemDescriptor", "FGResourceDescriptor"))) {
            String className = text(item, "ClassName");
            String displayName = text(item, "mDisplayName");
            if (isBlank(className) || isBlank(displayName)) {
                continue;
            }

            String key = snake(displayName);
            if (seenKeys.contains(key) || materialRepository.findByGameKey(key).isPresent()) {
                key = snake(cleanClassName(className));
            }
            seenKeys.add(key);

            MaterialType type = MATERIAL_FORM_MAP.getOrDefault(text(item, "mForm"), MaterialType.SOLID);
            classToMaterialKey.put(className, key);
            materialTypes.put(key, type);

            Material material = materialRepository.findByGameKey(key).orElseGet(Material::new);
            material.setGameKey(key);
            material.setName(displayName);
            material.setMaterialType(type);
            int mappedStack = STACK_SIZE_MAP.getOrDefault(text(item, "mStackSize"), type == MaterialType.SOLID ? 100 : 0);
            material.setStackSize(mappedStack == 0 ? null : mappedStack);
            material.setSinkable(bool(item, "mCanBeDiscarded", true));
            material.setEnabled(true);
            material.setDescription(nullIfBlank(text(item, "mDescription")));
            materialRepository.save(material);
            count++;
        }
        return count;
    }

    private int importRecipes(JsonNode docs, Map<String, String> classToMaterialKey, Map<String, MaterialType> materialTypes, String gameVersion) {
        int count = 0;
        for (JsonNode recipeNode : findClasses(docs, List.of("FGRecipe"))) {
            String className = text(recipeNode, "ClassName");
            String displayName = text(recipeNode, "mDisplayName");
            if (isBlank(className) || isBlank(displayName)) {
                continue;
            }

            String producedIn = text(recipeNode, "mProducedIn");
            String machineKey = machineKeyFromProducedIn(producedIn);
            if (machineKey == null) {
                continue;
            }

            List<MaterialAmount> inputs = parseAmounts(text(recipeNode, "mIngredients"), classToMaterialKey, materialTypes);
            List<MaterialAmount> outputs = parseAmounts(text(recipeNode, "mProduct"), classToMaterialKey, materialTypes);
            if (outputs.isEmpty()) {
                continue;
            }

            Machine machine = machineRepository.findByGameKey(machineKey)
                    .orElseGet(() -> machineRepository.findByGameKey("other").orElseThrow(() -> new BadRequestException("Machine not found: other")));

            String key = snake(className);
            Recipe recipe = recipeRepository.findByGameKey(key).orElseGet(Recipe::new);
            recipe.setGameKey(key);
            recipe.setName(displayName);
            recipe.setMachine(machine);
            recipe.setCycleTimeSeconds(durationSeconds(recipeNode));
            recipe.setAlternate(displayName.startsWith("Alternate:") || className.toLowerCase(Locale.ROOT).contains("alternate"));
            recipe.setSource("OFFICIAL_DOCS_JSON");
            recipe.setGameVersion(gameVersion);
            recipe.setEnabled(true);
            recipe.getInputs().clear();
            recipe.getOutputs().clear();

            for (MaterialAmount input : inputs) {
                materialRepository.findByGameKey(input.materialKey()).ifPresent(material -> {
                    RecipeInput entity = new RecipeInput();
                    entity.setRecipe(recipe);
                    entity.setMaterial(material);
                    entity.setAmountPerCycle(input.amountPerCycle());
                    recipe.getInputs().add(entity);
                });
            }
            for (MaterialAmount output : outputs) {
                materialRepository.findByGameKey(output.materialKey()).ifPresent(material -> {
                    RecipeOutput entity = new RecipeOutput();
                    entity.setRecipe(recipe);
                    entity.setMaterial(material);
                    entity.setAmountPerCycle(output.amountPerCycle());
                    recipe.getOutputs().add(entity);
                });
            }

            recipeRepository.save(recipe);
            count++;
        }
        return count;
    }

    private List<JsonNode> findClasses(JsonNode docs, List<String> nativeContains) {
        List<JsonNode> result = new ArrayList<>();
        for (JsonNode block : docs) {
            String nativeClass = text(block, "NativeClass");
            boolean match = nativeContains.stream().anyMatch(nativeClass::contains);
            if (!match) {
                continue;
            }
            JsonNode classes = block.get("Classes");
            if (classes != null && classes.isArray()) {
                for (JsonNode clazz : classes) {
                    result.add(clazz);
                }
            }
        }
        return result;
    }

    private List<MaterialAmount> parseAmounts(String raw, Map<String, String> classToMaterialKey, Map<String, MaterialType> materialTypes) {
        if (isBlank(raw)) {
            return List.of();
        }
        List<MaterialAmount> result = new ArrayList<>();
        Matcher matcher = ITEM_AMOUNT_PATTERN.matcher(raw);
        while (matcher.find()) {
            String className = matcher.group(1);
            String key = classToMaterialKey.get(className);
            if (key == null) {
                key = snake(cleanClassName(className));
            }
            double amount = Double.parseDouble(matcher.group(2));
            MaterialType type = materialTypes.get(key);
            if (type == MaterialType.FLUID || type == MaterialType.GAS) {
                amount = amount / 1000.0;
            }
            result.add(new MaterialAmount(key, amount));
        }
        return result;
    }

    private String machineKeyFromProducedIn(String raw) {
        if (isBlank(raw)) {
            return null;
        }
        for (Map.Entry<String, String> entry : PRODUCER_TO_MACHINE) {
            if (raw.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        // Skip hand-crafting/equipment-only recipes; they are not useful production building nodes.
        if (raw.contains("BP_Workshop") || raw.contains("Build_Workshop") || raw.contains("Build_WorkBench") || raw.contains("Build_Workbench")) {
            return null;
        }
        return "other";
    }

    private double durationSeconds(JsonNode recipeNode) {
        String a = text(recipeNode, "mManufactoringDuration");
        String b = text(recipeNode, "mManufacturingDuration");
        String value = !isBlank(a) ? a : b;
        if (isBlank(value)) {
            return 1.0;
        }
        return Double.parseDouble(value);
    }

    private static String text(JsonNode node, String field) {
        JsonNode value = node.get(field);
        if (value == null || value.isNull()) {
            return "";
        }
        return value.asText("");
    }

    private static boolean bool(JsonNode node, String field, boolean defaultValue) {
        JsonNode value = node.get(field);
        if (value == null || value.isNull()) {
            return defaultValue;
        }
        if (value.isBoolean()) {
            return value.asBoolean();
        }
        String text = value.asText();
        if (isBlank(text)) {
            return defaultValue;
        }
        return text.equalsIgnoreCase("true") || text.equals("1");
    }

    private static String snake(String value) {
        if (value == null) {
            return "";
        }
        String cleaned = value.replace("FICSIT", "Ficsit").replace("AI", "Ai");
        cleaned = cleaned.replaceAll("[^A-Za-z0-9]+", "_").replaceAll("^_+|_+$", "");
        cleaned = cleaned.replaceAll("([a-z0-9])([A-Z])", "$1_$2");
        return cleaned.toLowerCase(Locale.ROOT);
    }

    private static String cleanClassName(String className) {
        if (className == null) {
            return "";
        }
        return className.replaceFirst("^Desc_", "").replaceFirst("_C$", "").replaceFirst("^Recipe_", "");
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static String nullIfBlank(String value) {
        return isBlank(value) ? null : value;
    }

    private record MaterialAmount(String materialKey, double amountPerCycle) {}
}
