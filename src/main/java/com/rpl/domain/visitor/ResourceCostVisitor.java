package com.rpl.domain.visitor;

import com.rpl.domain.Plan;
import com.rpl.domain.ProposedAction;
import com.rpl.domain.ResourceAllocation;
import com.rpl.domain.composite.PlanNodeVisitor;
import java.math.BigDecimal;

public class ResourceCostVisitor implements PlanNodeVisitor {
    private BigDecimal totalCost = BigDecimal.ZERO;

    @Override
    public void visitLeaf(ProposedAction leaf) {
        for (ResourceAllocation a : leaf.getAllocations()) {
            BigDecimal unitCost = a.getResourceType().getUnitCost();
            if (unitCost != null) {
                totalCost = totalCost.add(a.getQuantity().multiply(unitCost));
            }
        }
    }

    @Override
    public void visitComposite(Plan plan) {
        plan.getAllChildren().forEach(c -> c.accept(this));
    }

    public BigDecimal getTotalCost() { return totalCost; }
}
