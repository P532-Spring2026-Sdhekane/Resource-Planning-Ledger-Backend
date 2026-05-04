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
                      && a.getResourceType().getKind() == ResourceType.Kind.ASSET)
            .collect(Collectors.toList());
    }

    @Override
    protected void validate(List<ResourceAllocation> allocs) {
        for (ResourceAllocation a : allocs) {
            if (a.getTimePeriodStart() == null || a.getTimePeriodEnd() == null) {
                throw new IllegalArgumentException(
                    "Asset allocation must have a non-null time period: " + a.getId());
            }
            long hours = ChronoUnit.HOURS.between(a.getTimePeriodStart(), a.getTimePeriodEnd());
            if (hours <= 0) {
                throw new IllegalArgumentException(
                    "Asset allocation time period must be positive hours, got: " + hours);
            }
        }
    }

    @Override
    protected Entry buildWithdrawal(Transaction tx, ResourceAllocation a) {
        long hours = ChronoUnit.HOURS.between(a.getTimePeriodStart(), a.getTimePeriodEnd());
        Entry e = new Entry();
        e.setAccount(a.getResourceType().getPoolAccount());
        e.setAmount(BigDecimal.valueOf(hours).negate());
        e.setEntryType(Entry.EntryType.WITHDRAWAL);
        e.setChargedAt(LocalDateTime.now());
        e.setOriginatingAction(a.getAction());
        e.setNotes("asset-hours:" + a.getAssetId());
        return e;
    }

    @Override
    protected Entry buildDeposit(Transaction tx, ResourceAllocation a) {
        long hours = ChronoUnit.HOURS.between(a.getTimePeriodStart(), a.getTimePeriodEnd());
        Entry e = new Entry();
        e.setAmount(BigDecimal.valueOf(hours));
        e.setEntryType(Entry.EntryType.DEPOSIT);
        e.setChargedAt(LocalDateTime.now());
        e.setOriginatingAction(a.getAction());
        e.setNotes("asset-usage:" + a.getResourceType().getId());
        return e;
    }

    @Override
    protected void afterPost(Transaction tx) {
        tx.getEntries().stream()
            .filter(e -> e.getNotes() != null && e.getNotes().startsWith("asset-hours:"))
            .forEach(e -> {
                String assetId = e.getNotes().replace("asset-hours:", "");
                AuditLogEntry log = new AuditLogEntry(
                    "ASSET_UTILISATION",
                    e.getOriginatingAction() != null ? e.getOriginatingAction().getId() : null,
                    "Asset " + assetId + " used for " + e.getAmount().abs() + " hours"
                );
                auditLogRepository.save(log);
            });
    }
}