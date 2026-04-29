package com.rpl.domain.iterator;

import com.rpl.domain.Plan;
import com.rpl.domain.composite.PlanNode;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class DepthFirstPlanIterator implements Iterator<PlanNode> {

    private final Deque<PlanNode> stack = new ArrayDeque<>();

    public DepthFirstPlanIterator(Plan root) {
        stack.push(root);
    }

    @Override
    public boolean hasNext() {
        return !stack.isEmpty();
    }

    @Override
    public PlanNode next() {
        if (!hasNext()) throw new NoSuchElementException();

        PlanNode node = stack.pop();

        if (node instanceof Plan plan) {
            List<PlanNode> children = plan.getAllChildren();
            for (int i = children.size() - 1; i >= 0; i--) {
                stack.push(children.get(i));
            }
        }

        return node;
    }
}
