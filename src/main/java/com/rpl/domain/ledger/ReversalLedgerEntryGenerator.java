package com.rpl.domain.ledger;

import com.rpl.domain.*;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ReversalLedgerEntryGenerator extends AbstractLedgerEntryGenerator {

    @Override
    protected List<ResourceAllocation> selectAllocations(ImplementedAction action) {
        return action.getProposedAction().getAllocations().stream()
            .filter(a -> a.getResourceType().getKind() == ResourceType.Kind.CONSUMABLE)
            .collect(Collectors.toList());
    }

    @Override
    protected void validate(List<ResourceAllocation> allocs) { }

    @Override
    protected Entry buildWithdrawal(Transaction tx, ResourceAllocation a) {
        Entry e = new Entry();
        e.setAccount(a.getResourceType().getPoolAccount());
        e.setAmount(a.getQuantity()); // positive = restore to pool
        e.setEntryType(Entry.EntryType.DEPOSIT);
        e.setChargedAt(LocalDateTime.now());
        e.setOriginatingAction(a.getAction());
        e.setNotes("reversal-credit");
        return e;
    }

    @Override
    protected Entry buildDeposit(Transaction tx, ResourceAllocation a) {
        Entry e = new Entry();
        e.setAmount(a.getQuantity().negate());
        e.setEntryType(Entry.EntryType.WITHDRAWAL);
        e.setChargedAt(LocalDateTime.now());
        e.setOriginatingAction(a.getAction());
        e.setNotes("reversal-debit:" + a.getResourceType().getId());
        return e;
    }
}
