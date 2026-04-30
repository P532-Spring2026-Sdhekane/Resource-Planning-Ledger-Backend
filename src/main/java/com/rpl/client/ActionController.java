package com.rpl.client;

import com.rpl.domain.ProposedAction;
import com.rpl.domain.ResourceAllocation;
import com.rpl.domain.ResourceType;
import com.rpl.manager.ActionManager;
import com.rpl.manager.ResourceTypeManager;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/actions")
public class ActionController {

    private final ActionManager actionManager;
    private final ResourceTypeManager resourceTypeManager;

    public ActionController(ActionManager actionManager, ResourceTypeManager resourceTypeManager) {
        this.actionManager = actionManager;
        this.resourceTypeManager = resourceTypeManager;
    }

    @GetMapping("/{id}")
    public ProposedAction get(@PathVariable Long id) {
        return actionManager.findById(id);
    }

    @PostMapping("/{id}/implement")
    public ProposedAction implement(@PathVariable Long id) {
        return actionManager.implement(id);
    }

    @PostMapping("/{id}/complete")
    public ProposedAction complete(@PathVariable Long id) {
        return actionManager.complete(id);
    }

    @PostMapping("/{id}/suspend")
    public ProposedAction suspend(@PathVariable Long id,
                                   @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ? body.getOrDefault("reason", "") : "";
        return actionManager.suspend(id, reason);
    }

    @PostMapping("/{id}/resume")
    public ProposedAction resume(@PathVariable Long id) {
        return actionManager.resume(id);
    }

    @PostMapping("/{id}/abandon")
    public ProposedAction abandon(@PathVariable Long id) {
        return actionManager.abandon(id);
    }

    @PostMapping("/{id}/allocations")
    public ProposedAction addAllocation(@PathVariable Long id,
                                         @RequestBody Map<String, Object> body) {
        Long resourceTypeId = Long.valueOf(body.get("resourceTypeId").toString());
        BigDecimal quantity  = new BigDecimal(body.get("quantity").toString());
        ResourceAllocation.AllocationKind kind = ResourceAllocation.AllocationKind.valueOf(
            body.getOrDefault("kind", "GENERAL").toString());
        String assetId = (String) body.get("assetId");
        return actionManager.addAllocation(id, resourceTypeId, quantity, kind, assetId);
    }

    @DeleteMapping("/{id}/allocations/{allocId}")
    public ProposedAction deleteAllocation(@PathVariable Long id,
                                           @PathVariable Long allocId) {
        return actionManager.deleteAllocation(id, allocId);
    }
}