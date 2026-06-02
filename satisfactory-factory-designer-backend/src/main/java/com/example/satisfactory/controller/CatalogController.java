package com.example.satisfactory.controller;

import com.example.satisfactory.dto.MachineDto;
import com.example.satisfactory.dto.MaterialDto;
import com.example.satisfactory.dto.RecipeDto;
import com.example.satisfactory.service.CatalogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CatalogController {
    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping("/materials")
    public List<MaterialDto> materials() {
        return catalogService.getMaterials();
    }

    @GetMapping("/machines")
    public List<MachineDto> machines() {
        return catalogService.getMachines();
    }

    @GetMapping("/recipes")
    public List<RecipeDto> recipes() {
        return catalogService.getRecipes();
    }
}
