package com.example.satisfactory.repository;

import com.example.satisfactory.entity.ProductionNode;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductionNodeRepository extends JpaRepository<ProductionNode, Long> {
    List<ProductionNode> findByBucketIdOrderBySortOrderAscIdAsc(Long bucketId);

    List<ProductionNode> findByBucketFactoryIdAndBucketEnabledTrueAndEnabledTrue(Long factoryId);
}
