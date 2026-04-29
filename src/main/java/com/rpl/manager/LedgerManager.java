package com.rpl.manager;

import com.rpl.domain.Account;
import com.rpl.domain.AuditLogEntry;
import com.rpl.domain.Entry;
import com.rpl.repository.AccountRepository;
import com.rpl.repository.AuditLogEntryRepository;
import com.rpl.repository.EntryRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.util.List;


@Service
@Transactional
public class LedgerManager {

    private final AccountRepository accountRepository;
    private final EntryRepository entryRepository;
    private final AuditLogEntryRepository auditLogRepository;

    public LedgerManager(AccountRepository accountRepository,
                         EntryRepository entryRepository,
                         AuditLogEntryRepository auditLogRepository) {
        this.accountRepository = accountRepository;
        this.entryRepository = entryRepository;
        this.auditLogRepository = auditLogRepository;
    }

    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    public Account findAccount(Long id) {
        return accountRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + id));
    }

    public List<Entry> getEntriesForAccount(Long accountId) {
        return entryRepository.findByAccountId(accountId);
    }

    public List<AuditLogEntry> getAuditLog() {
        return auditLogRepository.findAll();
    }
}
