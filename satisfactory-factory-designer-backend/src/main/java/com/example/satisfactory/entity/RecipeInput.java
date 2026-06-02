package com.example.satisfactory.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "recipe_inputs", uniqueConstraints = @UniqueConstraint(name = "uk_recipe_input_material", columnNames = {"recipe_id", "material_id"}))
public class RecipeInput {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipe_id", nullable = false)
    private Recipe recipe;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "material_id", nullable = false)
    private Material material;

    @Column(name = "amount_per_cycle", nullable = false)
    private Double amountPerCycle;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Recipe getRecipe() { return recipe; }
    public void setRecipe(Recipe recipe) { this.recipe = recipe; }
    public Material getMaterial() { return material; }
    public void setMaterial(Material material) { this.material = material; }
    public Double getAmountPerCycle() { return amountPerCycle; }
    public void setAmountPerCycle(Double amountPerCycle) { this.amountPerCycle = amountPerCycle; }
}
