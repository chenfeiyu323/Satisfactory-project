package com.example.satisfactory.controller;

import com.example.satisfactory.dto.MessageResponse;
import com.example.satisfactory.dto.ProductionNodeRequest;
import com.example.satisfactory.dto.ProductionNodeResponse;
import com.example.satisfactory.service.ProductionNodeService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ProductionNodeController {
    private final ProductionNodeService nodeService;

    public ProductionNodeController(ProductionNodeService nodeService) {
        this.nodeService = nodeService;
    }

    @GetMapping("/buckets/{bucketId}/nodes")
    public List<ProductionNodeResponse> list(@PathVariable Long bucketId) {
        return nodeService.findByBucket(bucketId);
    }

    @PostMapping("/buckets/{bucketId}/nodes")
    public ProductionNodeResponse create(@PathVariable Long bucketId, @Valid @RequestBody ProductionNodeRequest request) {
        return nodeService.create(bucketId, request);
    }

    @PatchMapping("/nodes/{nodeId}")
    public ProductionNodeResponse update(@PathVariable Long nodeId, @Valid @RequestBody ProductionNodeRequest request) {
        return nodeService.update(nodeId, request);
    }

    @DeleteMapping("/nodes/{nodeId}")
    public MessageResponse delete(@PathVariable Long nodeId) {
        nodeService.delete(nodeId);
        return new MessageResponse("Production node deleted.");
    }
}
