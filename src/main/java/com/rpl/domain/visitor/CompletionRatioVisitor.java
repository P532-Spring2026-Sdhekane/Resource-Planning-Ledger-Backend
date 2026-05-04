package com.rpl.domain.visitor;

import com.rpl.domain.Plan;
import com.rpl.domain.ProposedAction;
import com.rpl.domain.composite.ActionStatus;
import com.rpl.domain.composite.PlanNodeVisitor;

public class CompletionRatioVisitor implements PlanNodeVisitor {
    private int total = 0, completed = 0;

    @Override
    public void visitLeaf(ProposedAction leaf) {
        total++;
        if (leaf.getStatus() == ActionStatus.COMPLETED) completed++;
    }

    @Override
    public void visitComposite(Plan plan) {
        plan.getAllChildren().forEach(c -> c.accept(this));
    }

    public double getRatio() { return total == 0 ? 0.0 : (double) completed / total; }
    public int getTotal() { return total; }
    public int getCompleted() { return completed; }
}
