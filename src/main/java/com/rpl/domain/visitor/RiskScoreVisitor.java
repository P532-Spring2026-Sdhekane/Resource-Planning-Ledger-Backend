package com.rpl.domain.visitor;

import com.rpl.domain.Plan;
import com.rpl.domain.ProposedAction;
import com.rpl.domain.composite.ActionStatus;
import com.rpl.domain.composite.PlanNodeVisitor;

public class RiskScoreVisitor implements PlanNodeVisitor {
    private int score = 0;

    @Override
    public void visitLeaf(ProposedAction leaf) {
        if (leaf.getStatus() == ActionStatus.SUSPENDED
         || leaf.getStatus() == ActionStatus.ABANDONED) score++;
    }

    @Override
    public void visitComposite(Plan plan) {
        plan.getAllChildren().forEach(c -> c.accept(this));
    }

    public int getScore() { return score; }
}
