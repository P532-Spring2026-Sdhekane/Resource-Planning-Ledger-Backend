package com.rpl.manager;

import com.rpl.domain.*;
import com.rpl.domain.composite.ActionStatus;
import com.rpl.domain.ledger.ConsumableLedgerEntryGenerator;
import com.rpl.domain.state.*;
import com.rpl.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Manager layer: orchestrates action lifecycle use cases.
 * Uses the State pattern (ActionStateMachine) to drive transitions.
 * Implements LedgerCallback so state objects can trigger ledger operations.
 */
@Service
@Transactional
public class ActionManager implements ActionContext.LedgerCallback {

    private final ProposedActionRepository actionRepository;
    private final ImplementedActionRepository implementedActionRepository;
    private final SuspensionRepository suspensionRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final EntryRepository entryRepository;
    private final PostingRuleRepository postingRuleRepository;
    private final AuditLogEntryRepository auditLogRepository;
    private final ConsumableLedgerEntryGenerator ledgerGenerator;
    private final ResourceAllocationRepository allocationRepository;
    private final com.rpl.repository.ResourceTypeRepository resourceTypeRepository;

    // State singleton beans (injected by Spring)
    private final ProposedState proposedState;
    private final SuspendedState suspendedState;
    private final InProgressState inProgressState;
    private final CompletedState completedState;
    private final AbandonedState abandonedState;

    public ActionManager(ProposedActionRepository actionRepository,
                         ImplementedActionRepository implementedActionRepository,
                         SuspensionRepository suspensionRepository,
                         AccountRepository accountRepository,
                         TransactionRepository transactionRepository,
                         EntryRepository entryRepository,
                         PostingRuleRepository postingRuleRepository,
                         AuditLogEntryRepository auditLogRepository,
                         ConsumableLedgerEntryGenerator ledgerGenerator,
                         ResourceAllocationRepository allocationRepository,
                         com.rpl.repository.ResourceTypeRepository resourceTypeRepository,
                         ProposedState proposedState,
                         SuspendedState suspendedState,
                         InProgressState inProgressState,
                         CompletedState completedState,
                         AbandonedState abandonedState) {
        this.actionRepository = actionRepository;
        this.implementedActionRepository = implementedActionRepository;
        this.suspensionRepository = suspensionRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.entryRepository = entryRepository;
        this.postingRuleRepository = postingRuleRepository;
        this.auditLogRepository = auditLogRepository;
        this.ledgerGenerator = ledgerGenerator;
        this.allocationRepository = allocationRepository;
        this.resourceTypeRepository = resourceTypeRepository;
        this.proposedState = proposedState;
        this.suspendedState = suspendedState;
        this.inProgressState = inProgressState;
        this.completedState = completedState;
        this.abandonedState = abandonedState;
    }

    private ActionState resolveState(ProposedAction action) {
        return switch (action.getStatusEnum()) {
            case PROPOSED    -> proposedState;
            case SUSPENDED   -> suspendedState;
            case IN_PROGRESS -> inProgressState;
            case COMPLETED   -> completedState;
            case ABANDONED   -> abandonedState;
        };
    }

    private ActionContext contextFor(ProposedAction action) {
        return new ActionContext(action, this);
    }

    public ProposedAction findById(Long id) {
        return actionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Action not found: " + id));
    }

    public ProposedAction implement(Long actionId) {
        ProposedAction action = findById(actionId);
        ActionContext ctx = contextFor(action);
        resolveState(action).implement(ctx);

        // Create ImplementedAction
        ImplementedAction ia = new ImplementedAction(action);
        ia = implementedActionRepository.save(ia);
        action.setImplementedAction(ia);
        action = actionRepository.save(action);

        auditLog("IMPLEMENT", action.getId(), "Action moved to IN_PROGRESS");
        return action;
    }

    public ProposedAction complete(Long actionId) {
        ProposedAction action = findById(actionId);
        ActionContext ctx = contextFor(action);
        resolveState(action).complete(ctx);
        action = actionRepository.save(action);
        auditLog("COMPLETE", action.getId(), "Action completed, ledger entries generated");
        return action;
    }

    public ProposedAction suspend(Long actionId, String reason) {
        ProposedAction action = findById(actionId);
        ActionContext ctx = contextFor(action);
        resolveState(action).suspend(ctx, reason);
        action = actionRepository.save(action);
        auditLog("SUSPEND", action.getId(), "Action suspended: " + reason);
        return action;
    }

    public ProposedAction resume(Long actionId) {
        ProposedAction action = findById(actionId);
        ActionContext ctx = contextFor(action);
        resolveState(action).resume(ctx);
        action = actionRepository.save(action);
        auditLog("RESUME", action.getId(), "Action resumed to PROPOSED");
        return action;
    }

    public ProposedAction abandon(Long actionId) {
        ProposedAction action = findById(actionId);
        ActionContext ctx = contextFor(action);
        resolveState(action).abandon(ctx);
        action = actionRepository.save(action);
        auditLog("ABANDON", action.getId(), "Action abandoned");
        return action;
    }

    // --- LedgerCallback implementations ---

    @Override
    public void onComplete(ProposedAction action) {
        ImplementedAction ia = action.getImplementedAction();
        if (ia == null) return;

        // Skip ledger posting if no allocations (action has no resources to track)
        if (action.getAllocations().isEmpty()) {
            ia.setStatus(ActionStatus.COMPLETED);
            implementedActionRepository.save(ia);
            return;
        }

        // Use Template Method to generate entries
        Transaction tx = ledgerGenerator.generateEntries(ia);

        // Persist entries and update account balances
        for (Entry entry : tx.getEntries()) {
            Account account = entry.getAccount();
            if (entry.getEntryType() == Entry.EntryType.WITHDRAWAL) {
                account.debit(entry.getAmount().abs());
            } else {
                // For deposits to usage account, find or create usage account
                if (entry.getNotes() != null && entry.getNotes().startsWith("usage:")) {
                    Long rtId = Long.parseLong(entry.getNotes().split(":")[1]);
                    Account usageAccount = findOrCreateUsageAccount(rtId, action);
                    entry.setAccount(usageAccount);
                    usageAccount.credit(entry.getAmount());
                    accountRepository.save(usageAccount);
                }
            }
            accountRepository.save(account);

            // Check posting rules (over-consumption alert)
            checkPostingRules(account, entry, action);
        }

        transactionRepository.save(tx);

        // Update implemented action status
        ia.setStatus(ActionStatus.COMPLETED);
        implementedActionRepository.save(ia);
    }

    @Override
    public void onSuspend(ProposedAction action, String reason) {
        Suspension s = new Suspension(action, reason);
        suspensionRepository.save(s);
    }

    @Override
    public void onResume(ProposedAction action) {
        // Close open suspensions
        List<Suspension> open = suspensionRepository.findByProposedActionId(action.getId());
        open.stream()
            .filter(s -> s.getEndDate() == null)
            .forEach(s -> {
                s.setEndDate(java.time.LocalDate.now());
                suspensionRepository.save(s);
            });
    }

    private Account findOrCreateUsageAccount(Long resourceTypeId, ProposedAction action) {
        String name = "Usage-" + action.getId() + "-RT" + resourceTypeId;
        return accountRepository.findAll().stream()
            .filter(a -> a.getName().equals(name))
            .findFirst()
            .orElseGet(() -> {
                // Get resource type from allocations
                ResourceType rt = action.getAllocations().stream()
                    .filter(al -> al.getResourceType().getId().equals(resourceTypeId))
                    .map(ResourceAllocation::getResourceType)
                    .findFirst().orElseThrow();
                Account usageAccount = new Account(name, Account.Kind.USAGE, rt);
                return accountRepository.save(usageAccount);
            });
    }

    private void checkPostingRules(Account account, Entry triggeringEntry, ProposedAction action) {
        if (account.getBalance().compareTo(BigDecimal.ZERO) < 0) {
            List<PostingRule> rules = postingRuleRepository.findByTriggerAccountId(account.getId());
            for (PostingRule rule : rules) {
                if ("OVER_CONSUMPTION_ALERT".equals(rule.getStrategyType())) {
                    Entry alertEntry = new Entry();
                    alertEntry.setAccount(rule.getOutputAccount());
                    alertEntry.setAmount(account.getBalance().abs());
                    alertEntry.setEntryType(Entry.EntryType.ALERT);
                    alertEntry.setChargedAt(LocalDateTime.now());
                    alertEntry.setOriginatingAction(action);
                    alertEntry.setNotes("ALERT: Pool account " + account.getName() + " below zero");
                    entryRepository.save(alertEntry);

                    auditLog("ALERT", action.getId(),
                        "Over-consumption alert on account: " + account.getName());
                }
            }
        }
    }

    public ProposedAction addAllocation(Long actionId, Long resourceTypeId,
                                         BigDecimal quantity,
                                         ResourceAllocation.AllocationKind kind,
                                         String assetId) {
        ProposedAction action = findById(actionId);
        ResourceType rt = resourceTypeRepository.findById(resourceTypeId)
            .orElseThrow(() -> new ResourceNotFoundException("ResourceType not found: " + resourceTypeId));
        ResourceAllocation alloc = new ResourceAllocation(rt, quantity, kind);
        alloc.setAssetId(assetId);
        action.addAllocation(alloc);
        return actionRepository.save(action);
    }

    public ProposedAction deleteAllocation(Long actionId, Long allocId) {
        ProposedAction action = findById(actionId);
        action.getAllocations().removeIf(a -> a.getId().equals(allocId));
        allocationRepository.deleteById(allocId);
        return actionRepository.save(action);
    }

    private void auditLog(String event, Long actionId, String details) {
        AuditLogEntry log = new AuditLogEntry(event, actionId, details);
        auditLogRepository.save(log);
    }
}