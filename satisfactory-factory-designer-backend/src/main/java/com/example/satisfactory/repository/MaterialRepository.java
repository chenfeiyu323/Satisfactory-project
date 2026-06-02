package com.example.satisfactory.repository;

import com.example.satisfactory.entity.Material;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MaterialRepository extends JpaRepository<Material, Long> {
    Optional<Material> findByGameKey(String gameKey);
}
