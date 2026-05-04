package com.rpl.manager;

import com.rpl.domain.*;
import com.rpl.domain.composite.PlanNode;
import com.rpl.domain.visitor.*;
import java.util.Map;
import com.rpl.domain.iterator.DepthFirstPlanIterator;
import com.rpl.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


@Service
@Transactional
public class PlanManager {

    private final PlanRepository planRepository;
    private final ProposedActionRepository actionRepository;
    private final ProtocolRepository protocolRepository;

    public PlanManager(PlanRepository planRepository,
                       ProposedActionRepository actionRepository,
                       ProtocolRepository protocolRepository) {
        this.planRepository = planRepository;
        this.actionRepository = actionRepository;
        this.protocolRepository = protocolRepository;
    }

    public List<Plan> listAll() {
        return planRepository.findAll();
    }

    public Plan findById(Long id) {
        return planRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Plan not found: " + id));
    }

    
    public Plan createPlan(String name, LocalDate targetStartDate) {
        Plan plan = new Plan(name);
        plan.setTargetStartDate(targetStartDate);
        return planRepository.save(plan);
    }

    public Plan createFromProtocol(Long protocolId, String name, LocalDate targetStartDate) {
        Protocol protocol = protocolRepository.findById(protocolId)
            .orElseThrow(() -> new ResourceNotFoundException("Protocol not found: " + protocolId));

        Plan plan = new Plan(name != null ? name : protocol.getName() + " Plan");
        plan.setSourceProtocol(protocol);
        plan.setTargetStartDate(targetStartDate);

    
        List<ProtocolStep> steps = protocol.getSteps();
        List<ProposedAction> created = new ArrayList<>();
        for (ProtocolStep step : steps) {
            ProposedAction action = new ProposedAction(step.getName());
            action.setProtocol(step.getSubProtocol());
            action.setDependsOnNames(new ArrayList<>(step.getDependsOn()));
            plan.addAction(action);
            created.add(action);
        }

        return planRepository.save(plan);
    }

    public void addActionToPlan(Long planId, Long actionId) {
        Plan plan = findById(planId);
        ProposedAction action = actionRepository.findById(actionId)
            .orElseThrow(() -> new ResourceNotFoundException("Action not found: " + actionId));
        plan.addAction(action);
        planRepository.save(plan);
    }

    public ProposedAction createActionInPlan(Long planId, String name, String party, String location) {
        Plan plan = findById(planId);
        ProposedAction action = new ProposedAction(name);
        action.setParty(party);
        action.setLocation(location);
        plan.addAction(action);
        planRepository.save(plan);
        return action;
    }

   
    public List<ReportNode> generateReport(Long planId) {
        Plan plan = findById(planId); // triggers eager load
        List<ReportNode> report = new ArrayList<>();

        DepthFirstPlanIterator it = new DepthFirstPlanIterator(plan);
        while (it.hasNext()) {
            PlanNode node = it.next();
            report.add(new ReportNode(
                node.getId(),
                node.getName(),
                node.getNodeType(),
                node.getStatus().name()
            ));
        }
        return report;
    }

    public record ReportNode(Long id, String name, String type, String status) {}

    public Map<String, Object> getMetrics(Long planId) {
        Plan plan = findById(planId);
        CompletionRatioVisitor completion = new CompletionRatioVisitor();
        ResourceCostVisitor cost = new ResourceCostVisitor();
        RiskScoreVisitor risk = new RiskScoreVisitor();
        plan.accept(completion);
        plan.accept(cost);
        plan.accept(risk);
        return Map.of(
            "completionRatio", completion.getRatio(),
            "completedActions", completion.getCompleted(),
            "totalActions", completion.getTotal(),
            "totalResourceCost", cost.getTotalCost(),
            "riskScore", risk.getScore()
        );
    }
}