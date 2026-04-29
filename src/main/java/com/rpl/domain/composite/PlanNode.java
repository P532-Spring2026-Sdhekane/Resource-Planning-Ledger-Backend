package com.rpl.domain.composite;

import com.rpl.domain.ResourceType;
import java.math.BigDecimal;

public interface PlanNode {
    Long getId();
    String getName();
    String getNodeType(); 
    ActionStatus getStatus();
    BigDecimal getTotalAllocatedQuantity(ResourceType resourceType);
    void accept(PlanNodeVisitor visitor);
}
