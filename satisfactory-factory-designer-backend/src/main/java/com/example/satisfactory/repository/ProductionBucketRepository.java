package com.example.satisfactory.repository;

import com.example.satisfactory.entity.ProductionBucket;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductionBucketRepository extends JpaRepository<ProductionBucket, Long> {
    List<ProductionBucket> findByFactoryIdOrderBySortOrderAscIdAsc(Long factoryId);
    List<ProductionBucket> findByFactoryIdAndEnabledTrue(Long factoryId);
}
