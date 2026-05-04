package com.rpl.domain.iterator;

import com.rpl.domain.Plan;
import com.rpl.domain.composite.PlanNode;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;


public class LazySubtreeIterator implements Iterator<PlanNode> {

    private final Deque<int[]> stack = new ArrayDeque<>(); 
    private final Deque<PlanNode> nodeStack = new ArrayDeque<>();
    private final int depthLimit;

    // Store pairs of (node, depth)
    private record NodeDepth(PlanNode node, int depth) {}
    private final Deque<NodeDepth> queue = new ArrayDeque<>();

    public LazySubtreeIterator(PlanNode root, int depthLimit) {
        this.depthLimit = depthLimit;
        queue.push(new NodeDepth(root, 0));
    }

    @Override
    public boolean hasNext() { return !queue.isEmpty(); }

    @Override
    public PlanNode next() {
        if (!hasNext()) throw new NoSuchElementException();
        NodeDepth current = queue.pop();


        if (current.depth() < depthLimit && current.node() instanceof Plan plan) {
            var children = plan.getAllChildren();
            for (int i = children.size() - 1; i >= 0; i--) {
                queue.push(new NodeDepth(children.get(i), current.depth() + 1));
            }
        }

        return current.node();
    }
}