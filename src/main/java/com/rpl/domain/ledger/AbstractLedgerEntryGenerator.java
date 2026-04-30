package com.rpl.domain.ledger;

import com.rpl.domain.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Template Method pattern: defines the fixed skeleton for generating ledger entries.
 * Subclasses override selectAllocations(), validate(), and optionally buildWithdrawal(),
 * buildDeposit(), and afterPost().
 * postEntries() and createTransaction() are final — conservation is always enforced.
 */
public abstract class AbstractLedgerEntryGenerator {

    /**
     * Template method — final. Defines the invariant skeleton.
     * Subclasses extend behavior via abstract methods and hooks.
     */
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

    /** Subclass selects which allocations to process (e.g., only CONSUMABLE). */
    protected abstract List<ResourceAllocation> selectAllocations(ImplementedAction action);

    /** Subclass validates the selected allocations (e.g., positive quantity). */
    protected abstract void validate(List<ResourceAllocation> allocs);

    /** Build the withdrawal (debit from pool). Default: subtract from pool account. */
    protected Entry buildWithdrawal(Transaction tx, ResourceAllocation a) {
        Entry e = new Entry();
        e.setAccount(a.getResourceType().getPoolAccount());
        e.setAmount(a.getQuantity().negate());  // negative = withdrawal
        e.setEntryType(Entry.EntryType.WITHDRAWAL);
        e.setChargedAt(LocalDateTime.now());
        e.setOriginatingAction(a.getAction());
        return e;
    }

    /** Build the deposit (credit to usage account). Default: creates usage account entry. */
    protected Entry buildDeposit(Transaction tx, ResourceAllocation a) {
        Entry e = new Entry();
        // Usage account is looked up at post time; we set notes for now
        e.setAmount(a.getQuantity());
        e.setEntryType(Entry.EntryType.DEPOSIT);
        e.setChargedAt(LocalDateTime.now());
        e.setOriginatingAction(a.getAction());
        e.setNotes("usage:" + a.getResourceType().getId());
        return e;
    }

    /** Hook — empty in Week 1. Week 2 subclass overrides to write utilisation records. */
    protected void afterPost(Transaction tx) {
        // hook: no-op in base implementation
    }

    /**
     * Final: enforces double-entry conservation.
     * Adds both entries to the transaction. Cannot be overridden.
     */
    private void postEntries(Transaction tx, Entry withdrawal, Entry deposit) {
        // Conservation check: sum of all entries must net to zero
        BigDecimal sum = withdrawal.getAmount().add(deposit.getAmount());
        if (sum.compareTo(BigDecimal.ZERO) != 0) {
            throw new LedgerImbalanceException(
                "Double-entry imbalance: withdrawal=" + withdrawal.getAmount()
                + " deposit=" + deposit.getAmount());
        }
        tx.addEntry(withdrawal);
        tx.addEntry(deposit);
    }

    /** Final: creates the transaction record. Cannot be overridden. */
    private Transaction createTransaction(ImplementedAction action) {
        return new Transaction("Completion of action: " + action.getProposedAction().getName());
    }
}
