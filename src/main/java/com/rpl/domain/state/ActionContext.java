package com.rpl.domain.state;

import com.rpl.domain.ProposedAction;
import com.rpl.domain.composite.ActionStatus;


public class ActionContext {

    public interface LedgerCallback {
        void onComplete(ProposedAction action);
        void onSuspend(ProposedAction action, String reason);
        void onResume(ProposedAction action);
    }

    private final ProposedAction action;
    private final LedgerCallback callback;

    public ActionContext(ProposedAction action, LedgerCallback callback) {
        this.action = action;
        this.callback = callback;
    }

    public ProposedAction getAction() {
        return action;
    }

    public void setStatus(ActionStatus status) {
        action.setStatusEnum(status);
    }

    public ActionStatus getStatus() {
        return action.getStatusEnum();
    }

    public void triggerComplete() {
        callback.onComplete(action);
    }

    public void triggerSuspend(String reason) {
        callback.onSuspend(action, reason);
    }

    public void triggerResume() {
        callback.onResume(action);
    }
}
