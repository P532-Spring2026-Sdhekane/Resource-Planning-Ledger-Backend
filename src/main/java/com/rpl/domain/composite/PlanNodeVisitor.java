package com.rpl.domain.composite;

import com.rpl.domain.ProposedAction;
import com.rpl.domain.Plan;

public interface PlanNodeVisitor {
    void visitLeaf(ProposedAction action);
    void visitComposite(Plan plan);
}
