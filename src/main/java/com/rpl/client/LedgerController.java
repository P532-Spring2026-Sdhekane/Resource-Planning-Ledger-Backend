package com.rpl.client;

import com.rpl.domain.Account;
import com.rpl.domain.AuditLogEntry;
import com.rpl.domain.Entry;
import com.rpl.manager.LedgerManager;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
public class LedgerController {

    private final LedgerManager ledgerManager;

    public LedgerController(LedgerManager ledgerManager) {
        this.ledgerManager = ledgerManager;
    }

    @GetMapping("/api/accounts")
    public List<Account> getAllAccounts() {
        return ledgerManager.getAllAccounts();
    }

    @GetMapping("/api/accounts/{id}/entries")
    public List<Entry> getEntries(@PathVariable Long id) {
        return ledgerManager.getEntriesForAccount(id);
    }

    @GetMapping("/api/audit-log")
    public List<AuditLogEntry> getAuditLog() {
        return ledgerManager.getAuditLog();
    }
}
