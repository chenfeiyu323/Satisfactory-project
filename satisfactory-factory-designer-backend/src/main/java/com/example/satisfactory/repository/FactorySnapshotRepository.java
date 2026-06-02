package com.example.satisfactory.repository;

import com.example.satisfactory.entity.FactorySnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FactorySnapshotRepository extends JpaRepository<FactorySnapshot, Long> {
    List<FactorySnapshot> findByFactoryIdOrderByCreatedAtDesc(Long factoryId);
}
