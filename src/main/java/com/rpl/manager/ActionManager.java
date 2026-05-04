package com.rpl.manager;

import com.rpl.domain.*;
import com.rpl.domain.composite.ActionStatus;
import com.rpl.domain.ledger.AssetLedgerEntryGenerator;
import com.rpl.domain.ledger.ConsumableLedgerEntryGenerator;
import com.rpl.domain.ledger.ReversalLedgerEntryGenerator;
import com.rpl.domain.state.*;
import com.rpl.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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
    private final AssetLedgerEntryGenerator assetLedgerGenerator;
    private final ReversalLedgerEntryGenerator reversalLedgerGenerator;
    private final ResourceAllocationRepository allocationRepository;
    private final ResourceTypeRepository resourceTypeRepository;

    private final ProposedState proposedState;
    private final SuspendedState suspendedState;
    private final InProgressState inProgressState;
    private final CompletedState completedState;
    private final AbandonedState abandonedState;
    private final PendingApprovalState pendingApprovalState;
    private final ReopenedState reopenedState;

    public ActionManager(ProposedActionRepository actionRepository,
                         ImplementedActionRepository implementedActionRepository,
                         SuspensionRepository suspensionRepository,
                         AccountRepository accountRepository,
                         TransactionRepository transactionRepository,
                         EntryRepository entryRepository,
                         PostingRuleRepository postingRuleRepository,
                         AuditLogEntryRepository auditLogRepository,
                         ConsumableLedgerEntryGenerator ledgerGenerator,
                         AssetLedgerEntryGenerator assetLedgerGenerator,
                         ReversalLedgerEntryGenerator reversalLedgerGenerator,
                         ResourceAllocationRepository allocationRepository,
                         ResourceTypeRepository resourceTypeRepository,
                         ProposedState proposedState,
                         SuspendedState suspendedState,
                         InProgressState inProgressState,
                         CompletedState completedState,
                         AbandonedState abandonedState,
                         PendingApprovalState pendingApprovalState,
                         ReopenedState reopenedState) {
        this.actionRepository = actionRepository;
        this.implementedActionRepository = implementedActionRepository;
        this.suspensionRepository = suspensionRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.entryRepository = entryRepository;
        this.postingRuleRepository = postingRuleRepository;
        this.auditLogRepository = auditLogRepository;
        this.ledgerGenerator = ledgerGenerator;
        this.assetLedgerGenerator = assetLedgerGenerator;
        this.reversalLedgerGenerator = reversalLedgerGenerator;
        this.allocationRepository = allocationRepository;
        this.resourceTypeRepository = resourceTypeRepository;
        this.proposedState = proposedState;
        this.suspendedState = suspendedState;
        this.inProgressState = inProgressState;
        this.completedState = completedState;
        this.abandonedState = abandonedState;
        this.pendingApprovalState = pendingApprovalState;
        this.reopenedState = reopenedState;
    }

    private ActionState resolveState(ProposedAction action) {
        return switch (action.getStatusEnum()) {
            case PROPOSED         -> proposedState;
            case SUSPENDED        -> suspendedState;
            case IN_PROGRESS      -> inProgressState;
            case COMPLETED        -> completedState;
            case ABANDONED        -> abandonedState;
            case PENDING_APPROVAL -> pendingApprovalState;
            case REOPENED         -> reopenedState;
        };
    }

    private ActionContext contextFor(ProposedAction action) {
        return new ActionContext(action, this);
    }

    public ProposedAction findById(Long id) {
        return actionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Action not found: " + id));
    }

    // Week 1: kept for backward compat but now throws via ProposedState
    public ProposedAction implement(Long actionId) {
        ProposedAction action = findById(actionId);
        resolveState(action).implement(contextFor(action));
        return actionRepository.save(action);
    }

    // Week 2: new flow PROPOSED → PENDING_APPROVAL → IN_PROGRESS
    public ProposedAction submitForApproval(Long actionId) {
        ProposedAction action = findById(actionId);
        resolveState(action).submitForApproval(contextFor(action));
        action = actionRepository.save(action);
        auditLog("SUBMIT_FOR_APPROVAL", action.getId(), "Action submitted for approval");
        return action;
    }

    public ProposedAction approve(Long actionId) {
        ProposedAction action = findById(actionId);
        resolveState(action).approve(contextFor(action));
        // Create ImplementedAction on approval
        ImplementedAction ia = new ImplementedAction(action);
        ia = implementedActionRepository.save(ia);
        action.setImplementedAction(ia);
        action = actionRepository.save(action);
        auditLog("APPROVE", action.getId(), "Action approved → IN_PROGRESS");
        return action;
    }

    public ProposedAction reject(Long actionId) {
        ProposedAction action = findById(actionId);
        resolveState(action).reject(contextFor(action));
        action = actionRepository.save(action);
        auditLog("REJECT", action.getId(), "Action rejected → PROPOSED");
        return action;
    }

    public ProposedAction reopen(Long actionId) {
        ProposedAction action = findById(actionId);
        resolveState(action).reopen(contextFor(action));
        action = actionRepository.save(action);
        auditLog("REOPEN", action.getId(), "Action reopened from COMPLETED");
        return action;
    }

    public ProposedAction complete(Long actionId) {
        ProposedAction action = findById(actionId);
        resolveState(action).complete(contextFor(action));
        action = actionRepository.save(action);
        auditLog("COMPLETE", action.getId(), "Action completed");
        return action;
    }

    public ProposedAction suspend(Long actionId, String reason) {
        ProposedAction action = findById(actionId);
        resolveState(action).suspend(contextFor(action), reason);
        action = actionRepository.save(action);
        auditLog("SUSPEND", action.getId(), "Action suspended: " + reason);
        return action;
    }

    public ProposedAction resume(Long actionId) {
        ProposedAction action = findById(actionId);
        resolveState(action).resume(contextFor(action));
        action = actionRepository.save(action);
        auditLog("RESUME", action.getId(), "Action resumed");
        return action;
    }

    public ProposedAction abandon(Long actionId) {
        ProposedAction action = findById(actionId);
        resolveState(action).abandon(contextFor(action));
        action = actionRepository.save(action);
        auditLog("ABANDON", action.getId(), "Action abandoned");
        return action;
    }

    // --- LedgerCallback ---

    @Override
    public void onImplement(ProposedAction action) {
        // ImplementedAction creation is handled in approve()
    }

    @Override
    public void onComplete(ProposedAction action) {
        ImplementedAction ia = action.getImplementedAction();
        if (ia == null) return;

        if (action.getAllocations().isEmpty()) {
            ia.setStatus(ActionStatus.COMPLETED);
            implementedActionRepository.save(ia);
            return;
        }

        // Consumable entries (Template Method)
        Transaction tx = ledgerGenerator.generateEntries(ia);
        postTransaction(tx, action);

        // Asset entries (Template Method - Change 2)
        boolean hasAssetAllocs = action.getAllocations().stream()
            .anyMatch(a -> a.getKind() == ResourceAllocation.AllocationKind.SPECIFIC
                       && a.getResourceType().getKind() == ResourceType.Kind.ASSET
                       && a.getPeriodStart() != null);
        if (hasAssetAllocs) {
            Transaction assetTx = assetLedgerGenerator.generateEntries(ia);
            postTransaction(assetTx, action);
        }

        ia.setStatus(ActionStatus.COMPLETED);
        implementedActionRepository.save(ia);
    }

    @Override
    public void onReopen(ProposedAction action) {
        ImplementedAction ia = action.getImplementedAction();
        if (ia == null || action.getAllocations().isEmpty()) return;
        // Generate reversal entries to restore pool balances
        Transaction tx = reversalLedgerGenerator.generateEntries(ia);
        postTransaction(tx, action);
    }

    @Override
    public void onSuspend(ProposedAction action, String reason) {
        suspensionRepository.save(new Suspension(action, reason));
    }

    @Override
    public void onResume(ProposedAction action) {
        suspensionRepository.findByProposedActionId(action.getId()).stream()
            .filter(s -> s.getEndDate() == null)
            .forEach(s -> {
                s.setEndDate(java.time.LocalDate.now());
                suspensionRepository.save(s);
            });
    }

    private void postTransaction(Transaction tx, ProposedAction action) {
        for (Entry entry : tx.getEntries()) {
            Account account = entry.getAccount();
            if (account == null) {
                if (entry.getNotes() != null && entry.getNotes().startsWith("usage:")) {
                    Long rtId = Long.parseLong(entry.getNotes().split(":")[1]);
                    account = findOrCreateUsageAccount(rtId, action);
                    entry.setAccount(account);
                } else if (entry.getNotes() != null && entry.getNotes().startsWith("asset-usage:")) {
                    Long rtId = Long.parseLong(entry.getNotes().split(":")[1]);
                    account = findOrCreateUsageAccount(rtId, action);
                    entry.setAccount(account);
                }
            }
            if (account != null) {
                if (entry.getEntryType() == Entry.EntryType.WITHDRAWAL) {
                    account.debit(entry.getAmount().abs());
                } else if (entry.getEntryType() == Entry.EntryType.DEPOSIT) {
                    account.credit(entry.getAmount());
                }
                accountRepository.save(account);
                checkPostingRules(account, entry, action);
            }
        }
        transactionRepository.save(tx);
    }

    private Account findOrCreateUsageAccount(Long resourceTypeId, ProposedAction action) {
        String name = "Usage-" + action.getId() + "-RT" + resourceTypeId;
        return accountRepository.findAll().stream()
            .filter(a -> a.getName().equals(name))
            .findFirst()
            .orElseGet(() -> {
                ResourceType rt = action.getAllocations().stream()
                    .filter(al -> al.getResourceType().getId().equals(resourceTypeId))
                    .map(ResourceAllocation::getResourceType)
                    .findFirst().orElseThrow();
                return accountRepository.save(new Account(name, Account.Kind.USAGE, rt));
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
                    auditLog("ALERT", action.getId(), "Over-consumption alert: " + account.getName());
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
        auditLogRepository.save(new AuditLogEntry(event, actionId, details));
    }
}
