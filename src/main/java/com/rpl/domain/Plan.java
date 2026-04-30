package com.rpl.domain;

import com.rpl.domain.composite.ActionStatus;
import com.rpl.domain.composite.PlanNode;
import com.rpl.domain.composite.PlanNodeVisitor;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Composite node in the Composite pattern.
 * Derives its status from its children's statuses.
 * Children can be Plan (sub-plan) or ProposedAction (leaf).
 */
@Entity
@Table(name = "plans")
public class Plan implements PlanNode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_protocol_id")
    private Protocol sourceProtocol;

    private LocalDate targetStartDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_plan_id")
    private Plan parentPlan;

    // Sub-plan children
    @OneToMany(mappedBy = "parentPlan", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Plan> subPlans = new ArrayList<>();

    // Leaf children
    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<ProposedAction> actions = new ArrayList<>();

    public Plan() {}

    public Plan(String name) {
        this.name = name;
    }

    // --- PlanNode interface (Composite pattern) ---

    @Override
    public Long getId() { return id; }

    @Override
    public String getName() { return name; }

    @Override
    public String getNodeType() { return "PLAN"; }

    /**
     * Derived status from children:
     * - COMPLETED if all children completed
     * - IN_PROGRESS if any child is IN_PROGRESS or COMPLETED (not all completed)
     * - SUSPENDED if any child is SUSPENDED and none IN_PROGRESS
     * - ABANDONED if all children abandoned
     * - PROPOSED otherwise
     */
    @Override
    public ActionStatus getStatus() {
        List<PlanNode> children = getAllChildren();
        if (children.isEmpty()) return ActionStatus.PROPOSED;

        long total = children.size();
        long completed = children.stream().filter(c -> c.getStatus() == ActionStatus.COMPLETED).count();
        long abandoned = children.stream().filter(c -> c.getStatus() == ActionStatus.ABANDONED).count();
        long inProgress = children.stream().filter(c -> c.getStatus() == ActionStatus.IN_PROGRESS).count();
        long suspended  = children.stream().filter(c -> c.getStatus() == ActionStatus.SUSPENDED).count();

        if (completed == total) return ActionStatus.COMPLETED;
        if (abandoned == total) return ActionStatus.ABANDONED;
        if (inProgress > 0 || completed > 0) return ActionStatus.IN_PROGRESS;
        if (suspended > 0) return ActionStatus.SUSPENDED;
        return ActionStatus.PROPOSED;
    }

    @Override
    public BigDecimal getTotalAllocatedQuantity(ResourceType resourceType) {
        BigDecimal total = BigDecimal.ZERO;
        for (PlanNode child : getAllChildren()) {
            total = total.add(child.getTotalAllocatedQuantity(resourceType));
        }
        return total;
    }

    @Override
    public void accept(PlanNodeVisitor visitor) {
        visitor.visitComposite(this);
    }

    /** Returns all children (sub-plans + leaf actions) as PlanNode list. */
    public List<PlanNode> getAllChildren() {
        List<PlanNode> all = new ArrayList<>();
        all.addAll(subPlans);
        all.addAll(actions);
        return all;
    }

    // --- Getters / setters ---

    public void setName(String name) { this.name = name; }
    public Protocol getSourceProtocol() { return sourceProtocol; }
    public void setSourceProtocol(Protocol sourceProtocol) { this.sourceProtocol = sourceProtocol; }
    public LocalDate getTargetStartDate() { return targetStartDate; }
    public void setTargetStartDate(LocalDate targetStartDate) { this.targetStartDate = targetStartDate; }
    public Plan getParentPlan() { return parentPlan; }
    public void setParentPlan(Plan parentPlan) { this.parentPlan = parentPlan; }
    public List<Plan> getSubPlans() { return subPlans; }
    public List<ProposedAction> getActions() { return actions; }

    public void addAction(ProposedAction action) {
        action.setPlan(this);
        actions.add(action);
    }

    public void addSubPlan(Plan subPlan) {
        subPlan.setParentPlan(this);
        subPlans.add(subPlan);
    }
}
