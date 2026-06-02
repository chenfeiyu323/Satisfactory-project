package com.example.satisfactory.repository;

import com.example.satisfactory.entity.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {
    Optional<Recipe> findByGameKey(String gameKey);

    @Query("select distinct r from Recipe r left join fetch r.inputs left join fetch r.outputs where r.enabled = true")
    List<Recipe> findEnabledWithDetails();
}
