package com.rpl.domain.ledger;

import com.rpl.domain.*;
import com.rpl.repository.AuditLogEntryRepository;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class AssetLedgerEntryGenerator extends AbstractLedgerEntryGenerator {

    private final AuditLogEntryRepository auditLogRepository;

    public AssetLedgerEntryGenerator(AuditLogEntryRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    protected List<ResourceAllocation> selectAllocations(ImplementedAction action) {
        return action.getProposedAction().getAllocations().stream()
            .filter(a -> a.getKind() == ResourceAllocation.AllocationKind.SPECIFIC
                      && a.getResourceType().getKind() == ResourceType.Kind.ASSET
                      && a.getPeriodStart() != null && a.getPeriodEnd() != null)
            .collect(Collectors.toList());
    }

    @Override
    protected void validate(List<ResourceAllocation> allocs) {
        for (ResourceAllocation a : allocs) {
            if (a.getPeriodStart() == null || a.getPeriodEnd() == null) {
                throw new IllegalArgumentException("Asset allocation must have a time period: " + a.getId());
            }
            long days = java.time.temporal.ChronoUnit.DAYS.between(a.getPeriodStart(), a.getPeriodEnd());
            if (days <= 0) {
                throw new IllegalArgumentException("Asset allocation period must be positive: " + days);
            }
        }
    }

    @Override
    protected Entry buildWithdrawal(Transaction tx, ResourceAllocation a) {
        long days = java.time.temporal.ChronoUnit.DAYS.between(a.getPeriodStart(), a.getPeriodEnd());
        Entry e = new Entry();
        e.setAccount(a.getResourceType().getPoolAccount());
        e.setAmount(BigDecimal.valueOf(days).negate());
        e.setEntryType(Entry.EntryType.WITHDRAWAL);
        e.setChargedAt(LocalDateTime.now());
        e.setOriginatingAction(a.getAction());
        e.setNotes("asset-days:" + a.getAssetId());
        return e;
    }

    @Override
    protected Entry buildDeposit(Transaction tx, ResourceAllocation a) {
        long days = java.time.temporal.ChronoUnit.DAYS.between(a.getPeriodStart(), a.getPeriodEnd());
        Entry e = new Entry();
        e.setAmount(BigDecimal.valueOf(days));
        e.setEntryType(Entry.EntryType.DEPOSIT);
        e.setChargedAt(LocalDateTime.now());
        e.setOriginatingAction(a.getAction());
        e.setNotes("asset-usage:" + a.getResourceType().getId());
        return e;
    }

    @Override
    protected void afterPost(Transaction tx) {
        tx.getEntries().stream()
            .filter(e -> e.getNotes() != null && e.getNotes().startsWith("asset-days:"))
            .forEach(e -> {
                String assetId = e.getNotes().replace("asset-days:", "");
                AuditLogEntry log = new AuditLogEntry("ASSET_UTILISATION",
                    e.getOriginatingAction() != null ? e.getOriginatingAction().getId() : null,
                    "Asset " + assetId + " used for " + e.getAmount().abs() + " days");
                auditLogRepository.save(log);
            });
    }
}
