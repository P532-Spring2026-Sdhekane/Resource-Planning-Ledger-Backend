package com.rpl.client;

import com.rpl.domain.ResourceType;
import com.rpl.manager.ResourceTypeManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/resource-types")
public class ResourceTypeController {

    private final ResourceTypeManager resourceTypeManager;

    public ResourceTypeController(ResourceTypeManager resourceTypeManager) {
        this.resourceTypeManager = resourceTypeManager;
    }

    @GetMapping
    public List<ResourceType> list() {
        return resourceTypeManager.listAll();
    }

    @GetMapping("/{id}")
    public ResourceType get(@PathVariable Long id) {
        return resourceTypeManager.findById(id);
    }

    @PostMapping
    public ResourceType create(@RequestBody Map<String, String> body) {
        ResourceType.Kind kind = ResourceType.Kind.valueOf(body.get("kind"));
        return resourceTypeManager.create(body.get("name"), kind, body.get("unitOfMeasure"));
    }

    @PutMapping("/{id}")
    public ResourceType update(@PathVariable Long id, @RequestBody Map<String, String> body) {
        ResourceType.Kind kind = ResourceType.Kind.valueOf(body.get("kind"));
        return resourceTypeManager.update(id, body.get("name"), kind, body.get("unitOfMeasure"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        resourceTypeManager.delete(id);
        return ResponseEntity.noContent().build();
    }
}
