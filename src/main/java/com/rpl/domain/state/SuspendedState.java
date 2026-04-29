package com.rpl.domain.state;

import com.rpl.domain.composite.ActionStatus;
import org.springframework.stereotype.Component;


@Component
public class SuspendedState implements ActionState {

    @Override
    public void implement(ActionContext ctx) {
        throw new IllegalStateTransitionException(name(), "implement");
    }

    @Override
    public void suspend(ActionContext ctx, String reason) {
        throw new IllegalStateTransitionException(name(), "suspend");
    }

    @Override
    public void resume(ActionContext ctx) {
        ctx.setStatus(ActionStatus.PROPOSED);
        ctx.triggerResume();
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
    public String name() { return "SUSPENDED"; }
}
