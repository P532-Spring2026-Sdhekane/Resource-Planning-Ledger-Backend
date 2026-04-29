package com.rpl;

import com.rpl.domain.Plan;
import com.rpl.domain.ProposedAction;
import com.rpl.domain.composite.ActionStatus;
import com.rpl.domain.composite.PlanNode;
import com.rpl.domain.iterator.DepthFirstPlanIterator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class DepthFirstPlanIteratorTest {

    private ProposedAction leaf(String name) {
        ProposedAction a = new ProposedAction(name);
        a.setStatusEnum(ActionStatus.PROPOSED);
        return a;
    }

    private List<String> traverse(Plan root) {
        List<String> names = new ArrayList<>();
        DepthFirstPlanIterator it = new DepthFirstPlanIterator(root);
        while (it.hasNext()) names.add(it.next().getName());
        return names;
    }

    @Test
    void singleRootNoChildren_returnsRootOnly() {
        
        Plan root = new Plan("Root");
       
        List<String> names = traverse(root);
      
        assertEquals(List.of("Root"), names);
    }

    @Test
    void rootWithTwoLeaves_returnsRootThenLeavesInOrder() {
       
        Plan root = new Plan("Root");
        root.addAction(leaf("A"));
        root.addAction(leaf("B"));
     
        List<String> names = traverse(root);
 
        assertEquals(List.of("Root", "A", "B"), names);
    }

    @Test
    void nestedSubPlan_depthFirstOrder() {
      
        Plan root = new Plan("Root");
        Plan sub  = new Plan("Sub");
        sub.addAction(leaf("A"));
        sub.addAction(leaf("B"));
        root.addSubPlan(sub);
        root.addAction(leaf("C"));
        
        List<String> names = traverse(root);
       
        assertEquals(List.of("Root", "Sub", "A", "B", "C"), names);
    }

    @Test
    void deeplyNested_traversalIsDepthFirst() {
       
        Plan root = new Plan("Root");
        Plan sub1 = new Plan("Sub1");
        Plan sub2 = new Plan("Sub2");
        sub2.addAction(leaf("Leaf"));
        sub1.addSubPlan(sub2);
        root.addSubPlan(sub1);
      
        List<String> names = traverse(root);
      
        assertEquals(List.of("Root", "Sub1", "Sub2", "Leaf"), names);
    }

    @Test
    void hasNext_returnsFalseWhenExhausted() {
       
        Plan root = new Plan("Root");
        DepthFirstPlanIterator it = new DepthFirstPlanIterator(root);
        
        it.next();
        
        assertFalse(it.hasNext());
    }

    @Test
    void next_whenExhausted_throwsNoSuchElement() {
       
        Plan root = new Plan("Root");
        DepthFirstPlanIterator it = new DepthFirstPlanIterator(root);
        it.next(); 
      
        assertThrows(NoSuchElementException.class, it::next);
    }

    @Test
    void mixedChildrenOrder_subPlansBeforeLeaves() {
     
        Plan root = new Plan("Root");
        Plan sub  = new Plan("Sub");
        sub.addAction(leaf("SubLeaf"));
        root.addSubPlan(sub);
        root.addAction(leaf("RootLeaf"));
       
        List<String> names = traverse(root);
        
        assertEquals(List.of("Root", "Sub", "SubLeaf", "RootLeaf"), names);
    }

    @Test
    void totalNodeCount_matchesTreeSize() {
        
        Plan root = new Plan("Root");
        for (int i = 1; i <= 2; i++) {
            Plan sub = new Plan("Sub" + i);
            sub.addAction(leaf("Leaf" + i + "a"));
            sub.addAction(leaf("Leaf" + i + "b"));
            root.addSubPlan(sub);
        }
       
        List<String> names = traverse(root);
       
        assertEquals(7, names.size());
    }
}
