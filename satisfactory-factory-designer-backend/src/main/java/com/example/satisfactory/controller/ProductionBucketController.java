package com.example.satisfactory.controller;

import com.example.satisfactory.dto.MessageResponse;
import com.example.satisfactory.dto.ProductionBucketRequest;
import com.example.satisfactory.dto.ProductionBucketResponse;
import com.example.satisfactory.service.ProductionBucketService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ProductionBucketController {
    private final ProductionBucketService bucketService;

    public ProductionBucketController(ProductionBucketService bucketService) {
        this.bucketService = bucketService;
    }

    @GetMapping("/factories/{factoryId}/buckets")
    public List<ProductionBucketResponse> list(@PathVariable Long factoryId) {
        return bucketService.findByFactory(factoryId);
    }

    @PostMapping("/factories/{factoryId}/buckets")
    public ProductionBucketResponse create(@PathVariable Long factoryId, @Valid @RequestBody ProductionBucketRequest request) {
        return bucketService.create(factoryId, request);
    }

    @PatchMapping("/buckets/{bucketId}")
    public ProductionBucketResponse update(@PathVariable Long bucketId, @Valid @RequestBody ProductionBucketRequest request) {
        return bucketService.update(bucketId, request);
    }

    @DeleteMapping("/buckets/{bucketId}")
    public MessageResponse delete(@PathVariable Long bucketId) {
        bucketService.delete(bucketId);
        return new MessageResponse("Production bucket deleted.");
    }
}
