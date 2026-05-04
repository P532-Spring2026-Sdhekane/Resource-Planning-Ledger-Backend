package com.rpl.domain.state;

import com.rpl.domain.composite.ActionStatus;
import org.springframework.stereotype.Component;

/**
 * Stateless singleton bean representing PROPOSED state.
 * Legal transitions: implement(), suspend(), abandon().
 */
@Component
public class ProposedState implements ActionState {

    @Override
    public void implement(ActionContext ctx) {
        ctx.setStatus(ActionStatus.IN_PROGRESS);
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
