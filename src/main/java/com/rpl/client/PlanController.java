package com.rpl.client;

import com.rpl.domain.Plan;
import com.rpl.domain.ProposedAction;
import com.rpl.manager.PlanManager;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/plans")
public class PlanController {

    private final PlanManager planManager;

    public PlanController(PlanManager planManager) {
        this.planManager = planManager;
    }

    @GetMapping
    public List<Plan> list() {
        return planManager.listAll();
    }

    @GetMapping("/{id}")
    public Plan get(@PathVariable Long id) {
        return planManager.findById(id);
    }

    @PostMapping
    public Plan create(@RequestBody Map<String, Object> body) {
        String name = (String) body.get("name");
        LocalDate startDate = body.get("targetStartDate") != null
            ? LocalDate.parse((String) body.get("targetStartDate")) : null;

        if (body.containsKey("protocolId")) {
            Long protocolId = Long.valueOf(body.get("protocolId").toString());
            return planManager.createFromProtocol(protocolId, name, startDate);
        }
        return planManager.createPlan(name, startDate);
    }

    @GetMapping("/{id}/report")
    public List<PlanManager.ReportNode> report(@PathVariable Long id) {
        return planManager.generateReport(id);
    }

    @PostMapping("/{id}/actions")
    public ProposedAction addAction(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return planManager.createActionInPlan(id, body.get("name"), body.get("party"), body.get("location"));
    }
}
