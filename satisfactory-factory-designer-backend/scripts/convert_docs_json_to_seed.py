#!/usr/bin/env python3
"""
Convert Satisfactory CommunityResources/Docs/Docs.json into this project's seed files.

Usage:
  python scripts/convert_docs_json_to_seed.py "C:/Program Files (x86)/Steam/steamapps/common/Satisfactory/CommunityResources/Docs/Docs.json" src/main/resources/data/seed --game-version 1.0

Notes:
- Docs.json is shipped with the game and is the most reliable way to keep recipe data current.
- Fluids/gases in Docs.json are commonly represented in milli-units; this script divides FLUID/GAS amounts by 1000 for display/minute math.
- Machine mapping is best-effort; unknown production buildings are written as machineKey="other" unless a class-name rule below matches.
"""
from __future__ import annotations

import argparse
import json
import re
from pathlib import Path
from typing import Any

STACK_SIZE_MAP = {
    "SS_ONE": 1,
    "SS_SMALL": 50,
    "SS_MEDIUM": 100,
    "SS_BIG": 200,
    "SS_HUGE": 500,
    "SS_FLUID": None,
}

MATERIAL_FORM_MAP = {
    "RF_SOLID": "SOLID",
    "RF_LIQUID": "FLUID",
    "RF_GAS": "GAS",
    "RF_INVALID": "SOLID",
}

BASE_MACHINES = {
    "miner": ("Miner", "MINER", 5),
    "water_extractor": ("Water Extractor", "WATER_EXTRACTOR", 20),
    "oil_extractor": ("Oil Extractor", "OTHER", 40),
    "resource_well_extractor": ("Resource Well Extractor", "OTHER", 0),
    "smelter": ("Smelter", "SMELTER", 4),
    "foundry": ("Foundry", "FOUNDRY", 16),
    "constructor": ("Constructor", "CONSTRUCTOR", 4),
    "assembler": ("Assembler", "ASSEMBLER", 15),
    "manufacturer": ("Manufacturer", "MANUFACTURER", 55),
    "refinery": ("Refinery", "REFINERY", 30),
    "packager": ("Packager", "PACKAGER", 10),
    "blender": ("Blender", "BLENDER", 75),
    "particle_accelerator": ("Particle Accelerator", "PARTICLE_ACCELERATOR", 250),
    "converter": ("Converter", "OTHER", 250),
    "quantum_encoder": ("Quantum Encoder", "OTHER", 1000),
    "other": ("Other", "OTHER", 0),
}

PRODUCER_TO_MACHINE = [
    ("Build_Smelter", "smelter"),
    ("Build_Foundry", "foundry"),
    ("Build_Constructor", "constructor"),
    ("Build_Assembler", "assembler"),
    ("Build_Manufacturer", "manufacturer"),
    ("Build_OilRefinery", "refinery"),
    ("Build_Refinery", "refinery"),
    ("Build_Packager", "packager"),
    ("Build_Blender", "blender"),
    ("Build_HadronCollider", "particle_accelerator"),
    ("Build_ParticleAccelerator", "particle_accelerator"),
    ("Build_Converter", "converter"),
    ("Build_QuantumEncoder", "quantum_encoder"),
]

ITEM_REF_RE = re.compile(r"ItemClass=\"[^']*'[^.]+\.([^']+)'\",Amount=([0-9.]+)")


def snake(value: str) -> str:
    value = value.replace("FICSIT", "Ficsit").replace("AI", "Ai")
    value = re.sub(r"[^A-Za-z0-9]+", "_", value).strip("_")
    value = re.sub(r"([a-z0-9])([A-Z])", r"\1_\2", value)
    return value.lower()


def find_classes(docs: list[dict[str, Any]], native_contains: str) -> list[dict[str, Any]]:
    for block in docs:
        if native_contains in block.get("NativeClass", ""):
            return block.get("Classes", []) or []
    return []


def item_key_from_class(class_name: str, class_to_key: dict[str, str]) -> str | None:
    if class_name in class_to_key:
        return class_to_key[class_name]
    short = class_name
    short = re.sub(r"^Desc_", "", short)
    short = re.sub(r"_C$", "", short)
    return snake(short) if short else None


def parse_amounts(raw: str | None, class_to_key: dict[str, str], material_types: dict[str, str]) -> list[dict[str, Any]]:
    if not raw:
        return []
    results = []
    for class_name, amount_raw in ITEM_REF_RE.findall(raw):
        key = item_key_from_class(class_name, class_to_key)
        if not key:
            continue
        amount = float(amount_raw)
        if material_types.get(key) in {"FLUID", "GAS"}:
            amount /= 1000.0
        results.append({"materialKey": key, "amountPerCycle": amount})
    return results


def machine_key_from_produced_in(raw: str | None) -> str:
    raw = raw or ""
    for needle, machine_key in PRODUCER_TO_MACHINE:
        if needle in raw:
            return machine_key
    return "other"


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("docs_json", type=Path)
    parser.add_argument("output_dir", type=Path)
    parser.add_argument("--game-version", default="docs-json")
    args = parser.parse_args()

    docs = json.loads(args.docs_json.read_text(encoding="utf-8-sig"))
    out = args.output_dir
    out.mkdir(parents=True, exist_ok=True)

    # Materials
    item_classes = []
    for native_name in ["FGItemDescriptor", "FGResourceDescriptor", "FGItemDescriptorNuclearFuel", "FGItemDescriptorBiomass"]:
        item_classes.extend(find_classes(docs, native_name))

    class_to_key: dict[str, str] = {}
    materials = []
    for item in item_classes:
        class_name = item.get("ClassName")
        display = item.get("mDisplayName") or class_name
        if not class_name or not display:
            continue
        key = snake(display)
        if key in {m["gameKey"] for m in materials}:
            key = snake(re.sub(r"^Desc_", "", re.sub(r"_C$", "", class_name)))
        form = MATERIAL_FORM_MAP.get(item.get("mForm"), "SOLID")
        class_to_key[class_name] = key
        materials.append({
            "gameKey": key,
            "name": display,
            "materialType": form,
            "stackSize": STACK_SIZE_MAP.get(item.get("mStackSize"), 100 if form == "SOLID" else None),
            "sinkable": str(item.get("mCanBeDiscarded", "True")).lower() == "true",
            "enabled": True,
            "description": item.get("mDescription") or None,
        })
    material_types = {m["gameKey"]: m["materialType"] for m in materials}

    # Machines
    machines = [
        {"gameKey": k, "name": v[0], "machineType": v[1], "powerMw": v[2], "enabled": True}
        for k, v in BASE_MACHINES.items()
    ]

    # Recipes
    recipes = []
    seen_recipe_keys = set()
    recipe_classes = find_classes(docs, "FGRecipe")
    for recipe in recipe_classes:
        name = recipe.get("mDisplayName") or recipe.get("ClassName")
        class_name = recipe.get("ClassName") or snake(name)
        inputs = parse_amounts(recipe.get("mIngredients"), class_to_key, material_types)
        outputs = parse_amounts(recipe.get("mProduct"), class_to_key, material_types)
        if not outputs:
            continue
        machine_key = machine_key_from_produced_in(recipe.get("mProducedIn"))
        # Skip hand crafting / equipment-only recipes if desired by filtering unknown producers with no inputs.
        key = snake(class_name)
        if key in seen_recipe_keys:
            continue
        seen_recipe_keys.add(key)
        recipes.append({
            "gameKey": key,
            "name": name,
            "machineKey": machine_key,
            "cycleTimeSeconds": float(recipe.get("mManufactoringDuration") or recipe.get("mManufacturingDuration") or 1),
            "alternate": name.startswith("Alternate:") or "Alternate" in class_name,
            "source": "OFFICIAL_DOCS_JSON",
            "gameVersion": args.game_version,
            "enabled": True,
            "inputs": inputs,
            "outputs": outputs,
        })

    transport = [
        {"transportType": "BELT", "level": 1, "name": "Mk.1 Conveyor Belt", "capacityPerMin": 60, "sortOrder": 1},
        {"transportType": "BELT", "level": 2, "name": "Mk.2 Conveyor Belt", "capacityPerMin": 120, "sortOrder": 2},
        {"transportType": "BELT", "level": 3, "name": "Mk.3 Conveyor Belt", "capacityPerMin": 270, "sortOrder": 3},
        {"transportType": "BELT", "level": 4, "name": "Mk.4 Conveyor Belt", "capacityPerMin": 480, "sortOrder": 4},
        {"transportType": "BELT", "level": 5, "name": "Mk.5 Conveyor Belt", "capacityPerMin": 780, "sortOrder": 5},
        {"transportType": "BELT", "level": 6, "name": "Mk.6 Conveyor Belt", "capacityPerMin": 1200, "sortOrder": 6},
        {"transportType": "PIPE", "level": 1, "name": "Mk.1 Pipeline", "capacityPerMin": 300, "sortOrder": 1},
        {"transportType": "PIPE", "level": 2, "name": "Mk.2 Pipeline", "capacityPerMin": 600, "sortOrder": 2},
    ]

    for filename, data in [
        ("materials.json", materials),
        ("machines.json", machines),
        ("recipes.json", recipes),
        ("transport_levels.json", transport),
    ]:
        (out / filename).write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")

    print(f"Generated {len(materials)} materials, {len(machines)} machines, {len(recipes)} recipes, {len(transport)} transport levels into {out}")


if __name__ == "__main__":
    main()
