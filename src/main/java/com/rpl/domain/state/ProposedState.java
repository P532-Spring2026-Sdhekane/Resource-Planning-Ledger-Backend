package com.rpl.domain.state;

import com.rpl.domain.composite.ActionStatus;
import org.springframework.stereotype.Component;

@Component
public class ProposedState implements ActionState {

    @Override
    public void implement(ActionContext ctx) {
        throw new IllegalStateTransitionException(name(), "implement");
    }

    @Override
    public void submitForApproval(ActionContext ctx) {
        ctx.setStatus(ActionStatus.PENDING_APPROVAL);
    }

    @Override
    public void approve(ActionContext ctx) {
        throw new IllegalStateTransitionException(name(), "approve");
    }

    @Override
    public void reject(ActionContext ctx) {
        throw new IllegalStateTransitionException(name(), "reject");
    }

    @Override
    public void reopen(ActionContext ctx) {
        throw new IllegalStateTransitionException(name(), "reopen");
    }

    @Override
    public void suspend(ActionContext ctx, String reason) {
        ctx.setStatus(ActionStatus.SUSPENDED);
        ctx.triggerSuspend(reason);
    }

    @Override
    public void resume(ActionContext ctx) {
        throw new IllegalStateTransitionException(name(), "resume");
    }

    @Override
    public void complete(ActionContext ctx) {
        throw new IllegalStateTransitionException(name(), "complete");
    }

    @Override
    public void abandon(ActionContext ctx) {
        ctx.setStatus(ActionStatus.ABANDONED);
    }

    @Override
    public String name() { return "PROPOSED"; }
}
