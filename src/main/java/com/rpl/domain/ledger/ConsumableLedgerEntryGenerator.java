package com.rpl.domain.ledger;

import com.rpl.domain.ImplementedAction;
import com.rpl.domain.ResourceAllocation;
import com.rpl.domain.ResourceType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ConsumableLedgerEntryGenerator extends AbstractLedgerEntryGenerator {

    @Override
    protected List<ResourceAllocation> selectAllocations(ImplementedAction action) {
        return action.getProposedAction().getAllocations().stream()
            .filter(a -> a.getResourceType().getKind() == ResourceType.Kind.CONSUMABLE)
            .collect(Collectors.toList());
    }

    @Override
    protected void validate(List<ResourceAllocation> allocs) {
        for (ResourceAllocation a : allocs) {
            if (a.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException(
                    "Consumable allocation must have positive quantity, got: " + a.getQuantity());
            }
        }
    }
}
