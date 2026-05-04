package com.rpl.domain.state;

import com.rpl.domain.composite.ActionStatus;
import org.springframework.stereotype.Component;

@Component
public class PendingApprovalState implements ActionState {

    @Override
    public void implement(ActionContext ctx) {
        throw new IllegalStateTransitionException(name(), "implement");
    }

    @Override
    public void submitForApproval(ActionContext ctx) {
        throw new IllegalStateTransitionException(name(), "submitForApproval");
    }

    @Override
    public void approve(ActionContext ctx) {
        ctx.setStatus(ActionStatus.IN_PROGRESS);
        ctx.triggerImplement();
    }

    @Override
    public void reject(ActionContext ctx) {
        ctx.setStatus(ActionStatus.PROPOSED);
    }

    @Override
    public void reopen(ActionContext ctx) {
        throw new IllegalStateTransitionException(name(), "reopen");
    }

    @Override
    public void suspend(ActionContext ctx, String reason) {
        throw new IllegalStateTransitionException(name(), "suspend");
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
    public String name() { return "PENDING_APPROVAL"; }
}
