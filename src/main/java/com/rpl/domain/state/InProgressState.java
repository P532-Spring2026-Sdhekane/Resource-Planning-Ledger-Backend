package com.rpl.domain.state;

import com.rpl.domain.composite.ActionStatus;
import org.springframework.stereotype.Component;

/**
 * Stateless singleton bean representing IN_PROGRESS state.
 * Legal transitions: complete(), suspend(), abandon().
 */
@Component
public class InProgressState implements ActionState {


    @Override
    public void submitForApproval(ActionContext ctx) {
        throw new IllegalStateTransitionException(name(), "submitForApproval");
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
    public void implement(ActionContext ctx) {
        throw new IllegalStateTransitionException(name(), "implement");
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
        ctx.setStatus(ActionStatus.COMPLETED);
        ctx.triggerComplete();
    }

    @Override
    public void abandon(ActionContext ctx) {
        ctx.setStatus(ActionStatus.ABANDONED);
    }

    @Override
    public String name() { return "IN_PROGRESS"; }
}
