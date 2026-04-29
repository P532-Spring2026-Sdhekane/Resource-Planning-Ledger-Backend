package com.rpl.domain.ledger;

import com.rpl.domain.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


public abstract class AbstractLedgerEntryGenerator {

    public final Transaction generateEntries(ImplementedAction action) {
        List<ResourceAllocation> allocs = selectAllocations(action);
        validate(allocs);
        Transaction tx = createTransaction(action);

        for (ResourceAllocation a : allocs) {
            Entry withdrawal = buildWithdrawal(tx, a);
            Entry deposit    = buildDeposit(tx, a);
            postEntries(tx, withdrawal, deposit);
        }

        afterPost(tx);
        return tx;
    }

  
    protected abstract List<ResourceAllocation> selectAllocations(ImplementedAction action);

   
    protected abstract void validate(List<ResourceAllocation> allocs);

    protected Entry buildWithdrawal(Transaction tx, ResourceAllocation a) {
        Entry e = new Entry();
        e.setAccount(a.getResourceType().getPoolAccount());
        e.setAmount(a.getQuantity().negate()); 
        e.setEntryType(Entry.EntryType.WITHDRAWAL);
        e.setChargedAt(LocalDateTime.now());
        e.setOriginatingAction(a.getAction());
        return e;
    }

  
    protected Entry buildDeposit(Transaction tx, ResourceAllocation a) {
        Entry e = new Entry();
        e.setAmount(a.getQuantity());
        e.setEntryType(Entry.EntryType.DEPOSIT);
        e.setChargedAt(LocalDateTime.now());
        e.setOriginatingAction(a.getAction());
        e.setNotes("usage:" + a.getResourceType().getId());
        return e;
    }

   
    protected void afterPost(Transaction tx) {
       
    }

    private void postEntries(Transaction tx, Entry withdrawal, Entry deposit) {
       
        BigDecimal sum = withdrawal.getAmount().add(deposit.getAmount());
        if (sum.compareTo(BigDecimal.ZERO) != 0) {
            throw new LedgerImbalanceException(
                "Double-entry imbalance: withdrawal=" + withdrawal.getAmount()
                + " deposit=" + deposit.getAmount());
        }
        tx.addEntry(withdrawal);
        tx.addEntry(deposit);
    }

    private Transaction createTransaction(ImplementedAction action) {
        return new Transaction("Completion of action: " + action.getProposedAction().getName());
    }
}
