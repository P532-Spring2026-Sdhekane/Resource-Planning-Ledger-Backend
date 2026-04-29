package com.rpl.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_log_entries")
public class AuditLogEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String event;
    private Long accountId;
    private Long entryId;
    private Long actionId;
    private LocalDateTime timestamp;
    private String details;

    public AuditLogEntry() {
        this.timestamp = LocalDateTime.now();
    }

    public AuditLogEntry(String event, Long actionId, String details) {
        this();
        this.event = event;
        this.actionId = actionId;
        this.details = details;
    }

    public Long getId() { return id; }
    public String getEvent() { return event; }
    public void setEvent(String event) { this.event = event; }
    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }
    public Long getEntryId() { return entryId; }
    public void setEntryId(Long entryId) { this.entryId = entryId; }
    public Long getActionId() { return actionId; }
    public void setActionId(Long actionId) { this.actionId = actionId; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
}
