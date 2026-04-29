package com.rpl;

import com.rpl.domain.Plan;
import com.rpl.domain.ProposedAction;
import com.rpl.domain.composite.ActionStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
class CompositeStatusTest {

    private ProposedAction actionWithStatus(String name, ActionStatus s) {
        ProposedAction a = new ProposedAction(name);
        a.setStatusEnum(s);
        return a;
    }

    @Test
    void emptyPlan_returnsProposed() {
        
        Plan plan = new Plan("Empty");
       
        assertEquals(ActionStatus.PROPOSED, plan.getStatus());
    }

    @Test
    void allChildrenCompleted_returnsCompleted() {
       
        Plan plan = new Plan("All Done");
        plan.addAction(actionWithStatus("A", ActionStatus.COMPLETED));
        plan.addAction(actionWithStatus("B", ActionStatus.COMPLETED));
      
        assertEquals(ActionStatus.COMPLETED, plan.getStatus());
    }

    @Test
    void allChildrenAbandoned_returnsAbandoned() {
      
        Plan plan = new Plan("All Abandoned");
        plan.addAction(actionWithStatus("A", ActionStatus.ABANDONED));
        plan.addAction(actionWithStatus("B", ActionStatus.ABANDONED));
      
        assertEquals(ActionStatus.ABANDONED, plan.getStatus());
    }

    @Test
    void anyChildInProgress_returnsInProgress() {
      
        Plan plan = new Plan("Partial");
        plan.addAction(actionWithStatus("A", ActionStatus.PROPOSED));
        plan.addAction(actionWithStatus("B", ActionStatus.IN_PROGRESS));
      
        assertEquals(ActionStatus.IN_PROGRESS, plan.getStatus());
    }

    @Test
    void someCompletedNoneInProgress_returnsInProgress() {
        
        Plan plan = new Plan("Almost");
        plan.addAction(actionWithStatus("A", ActionStatus.COMPLETED));
        plan.addAction(actionWithStatus("B", ActionStatus.PROPOSED));
       
        assertEquals(ActionStatus.IN_PROGRESS, plan.getStatus());
    }

    @Test
    void anySuspendedNoneInProgress_returnsSuspended() {

        Plan plan = new Plan("Paused");
        plan.addAction(actionWithStatus("A", ActionStatus.SUSPENDED));
        plan.addAction(actionWithStatus("B", ActionStatus.PROPOSED));

        assertEquals(ActionStatus.SUSPENDED, plan.getStatus());
    }

    @Test
    void nestedSubPlanCompleted_parentDrivesFromSubPlanStatus() {
  
        Plan parent = new Plan("Parent");
        Plan child  = new Plan("Child");
        child.addAction(actionWithStatus("A", ActionStatus.COMPLETED));
        parent.addSubPlan(child);

        assertEquals(ActionStatus.COMPLETED, parent.getStatus());
    }
}
