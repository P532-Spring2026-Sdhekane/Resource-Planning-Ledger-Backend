package com.rpl.client;

import com.rpl.domain.Protocol;
import com.rpl.manager.ProtocolManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/protocols")
public class ProtocolController {

    private final ProtocolManager protocolManager;

    public ProtocolController(ProtocolManager protocolManager) {
        this.protocolManager = protocolManager;
    }

    @GetMapping
    public List<Protocol> list() {
        return protocolManager.listAll();
    }

    @GetMapping("/{id}")
    public Protocol get(@PathVariable Long id) {
        return protocolManager.findById(id);
    }

    @PostMapping
    public Protocol create(@RequestBody Map<String, String> body) {
        return protocolManager.create(body.get("name"), body.get("description"));
    }

    @PutMapping("/{id}")
    public Protocol update(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return protocolManager.update(id, body.get("name"), body.get("description"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        protocolManager.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/steps")
    public Protocol addStep(@PathVariable Long id, @RequestBody StepRequest req) {
        return protocolManager.addStep(id, req.name(), req.subProtocolId(), req.dependsOn());
    }

    public record StepRequest(String name, Long subProtocolId, List<String> dependsOn) {}
}
