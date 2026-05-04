package com.rpl;

import com.rpl.domain.ProposedAction;
import com.rpl.domain.composite.ActionStatus;
import com.rpl.domain.state.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActionStateMachineTest {

    private ProposedState proposedState;
    private SuspendedState suspendedState;
    private InProgressState inProgressState;
    private CompletedState completedState;
    private AbandonedState abandonedState;
    private PendingApprovalState pendingApprovalState;
    private ReopenedState reopenedState;

    @Mock
    private ActionContext.LedgerCallback ledgerCallback;

    private ProposedAction action;
    private ActionContext ctx;

    @BeforeEach
    void setUp() {
        proposedState        = new ProposedState();
        suspendedState       = new SuspendedState();
        inProgressState      = new InProgressState();
        completedState       = new CompletedState();
        abandonedState       = new AbandonedState();
        pendingApprovalState = new PendingApprovalState();
        reopenedState        = new ReopenedState();

        action = new ProposedAction("Test Action");
        ctx = new ActionContext(action, ledgerCallback);
    }

    // Week 2: implement() from PROPOSED now throws
    @Test
    void implement_fromProposed_throwsIllegalStateTransition() {
        action.setStatusEnum(ActionStatus.PROPOSED);
        assertThrows(IllegalStateTransitionException.class,
            () -> proposedState.implement(ctx));
    }

    // Week 2: new approval flow
    @Test
    void submitForApproval_fromProposed_movesToPendingApproval() {
        action.setStatusEnum(ActionStatus.PROPOSED);
        proposedState.submitForApproval(ctx);
        assertEquals(ActionStatus.PENDING_APPROVAL, action.getStatusEnum());
    }

    @Test
    void approve_fromPendingApproval_movesToInProgress() {
        action.setStatusEnum(ActionStatus.PENDING_APPROVAL);
        pendingApprovalState.approve(ctx);
        assertEquals(ActionStatus.IN_PROGRESS, action.getStatusEnum());
        verify(ledgerCallback).onImplement(action);
    }

    @Test
    void reject_fromPendingApproval_movesToProposed() {
        action.setStatusEnum(ActionStatus.PENDING_APPROVAL);
        pendingApprovalState.reject(ctx);
        assertEquals(ActionStatus.PROPOSED, action.getStatusEnum());
    }

    // Week 2: reopen flow
    @Test
    void reopen_fromCompleted_movesToReopened() {
        action.setStatusEnum(ActionStatus.COMPLETED);
        completedState.reopen(ctx);
        assertEquals(ActionStatus.REOPENED, action.getStatusEnum());
        verify(ledgerCallback).onReopen(action);
    }

    @Test
    void complete_fromReopened_movesToCompleted() {
        action.setStatusEnum(ActionStatus.REOPENED);
        reopenedState.complete(ctx);
        assertEquals(ActionStatus.COMPLETED, action.getStatusEnum());
        verify(ledgerCallback).onComplete(action);
    }

    @Test
    void abandon_fromReopened_movesToAbandoned() {
        action.setStatusEnum(ActionStatus.REOPENED);
        reopenedState.abandon(ctx);
        assertEquals(ActionStatus.ABANDONED, action.getStatusEnum());
    }

    // Week 1 transitions still valid
    @Test
    void suspend_fromProposed_movesToSuspendedAndCallsCallback() {
        action.setStatusEnum(ActionStatus.PROPOSED);
        proposedState.suspend(ctx, "waiting for resources");
        assertEquals(ActionStatus.SUSPENDED, action.getStatusEnum());
        verify(ledgerCallback).onSuspend(action, "waiting for resources");
    }

    @Test
    void abandon_fromProposed_movesToAbandoned() {
        action.setStatusEnum(ActionStatus.PROPOSED);
        proposedState.abandon(ctx);
        assertEquals(ActionStatus.ABANDONED, action.getStatusEnum());
    }

    @Test
    void resume_fromSuspended_movesToProposed() {
        action.setStatusEnum(ActionStatus.SUSPENDED);
        suspendedState.resume(ctx);
        assertEquals(ActionStatus.PROPOSED, action.getStatusEnum());
        verify(ledgerCallback).onResume(action);
    }

    @Test
    void abandon_fromSuspended_movesToAbandoned() {
        action.setStatusEnum(ActionStatus.SUSPENDED);
        suspendedState.abandon(ctx);
        assertEquals(ActionStatus.ABANDONED, action.getStatusEnum());
    }

    @Test
    void complete_fromInProgress_movesToCompletedAndCallsCallback() {
        action.setStatusEnum(ActionStatus.IN_PROGRESS);
        inProgressState.complete(ctx);
        assertEquals(ActionStatus.COMPLETED, action.getStatusEnum());
        verify(ledgerCallback).onComplete(action);
    }

    @Test
    void suspend_fromInProgress_movesToSuspended() {
        action.setStatusEnum(ActionStatus.IN_PROGRESS);
        inProgressState.suspend(ctx, "blocking issue");
        assertEquals(ActionStatus.SUSPENDED, action.getStatusEnum());
    }

    @Test
    void abandon_fromInProgress_movesToAbandoned() {
        action.setStatusEnum(ActionStatus.IN_PROGRESS);
        inProgressState.abandon(ctx);
        assertEquals(ActionStatus.ABANDONED, action.getStatusEnum());
    }

    // Illegal transitions
    @Test
    void resume_fromProposed_throwsIllegalStateTransition() {
        action.setStatusEnum(ActionStatus.PROPOSED);
        assertThrows(IllegalStateTransitionException.class,
            () -> proposedState.resume(ctx));
    }

    @Test
    void complete_fromProposed_throwsIllegalStateTransition() {
        action.setStatusEnum(ActionStatus.PROPOSED);
        assertThrows(IllegalStateTransitionException.class,
            () -> proposedState.complete(ctx));
    }

    @Test
    void implement_fromSuspended_throwsIllegalStateTransition() {
        action.setStatusEnum(ActionStatus.SUSPENDED);
        assertThrows(IllegalStateTransitionException.class,
            () -> suspendedState.implement(ctx));
    }

    @Test
    void implement_fromCompleted_throwsIllegalStateTransition() {
        action.setStatusEnum(ActionStatus.COMPLETED);
        assertThrows(IllegalStateTransitionException.class,
            () -> completedState.implement(ctx));
    }

    @Test
    void abandon_fromCompleted_throwsIllegalStateTransition() {
        action.setStatusEnum(ActionStatus.COMPLETED);
        assertThrows(IllegalStateTransitionException.class,
            () -> completedState.abandon(ctx));
    }

    @Test
    void anyTransition_fromAbandoned_throwsIllegalStateTransition() {
        action.setStatusEnum(ActionStatus.ABANDONED);
        assertThrows(IllegalStateTransitionException.class, () -> abandonedState.implement(ctx));
        assertThrows(IllegalStateTransitionException.class, () -> abandonedState.suspend(ctx, "x"));
        assertThrows(IllegalStateTransitionException.class, () -> abandonedState.resume(ctx));
        assertThrows(IllegalStateTransitionException.class, () -> abandonedState.complete(ctx));
        assertThrows(IllegalStateTransitionException.class, () -> abandonedState.abandon(ctx));
    }

    @Test
    void resume_fromInProgress_throwsIllegalStateTransition() {
        action.setStatusEnum(ActionStatus.IN_PROGRESS);
        assertThrows(IllegalStateTransitionException.class,
            () -> inProgressState.resume(ctx));
    }
}