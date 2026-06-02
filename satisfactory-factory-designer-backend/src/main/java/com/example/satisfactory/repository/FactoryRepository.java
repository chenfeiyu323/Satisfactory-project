package com.example.satisfactory.repository;

import com.example.satisfactory.entity.Factory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FactoryRepository extends JpaRepository<Factory, Long> {
}
