package com.rpl.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "entries")
public class Entry {

    public enum EntryType { WITHDRAWAL, DEPOSIT, ALERT }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id")
    private Transaction transaction;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private EntryType entryType;

    private LocalDateTime chargedAt;
    private LocalDateTime bookedAt;

    // Which action triggered this
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "originating_action_id")
    private ProposedAction originatingAction;

    @Column(columnDefinition = "TEXT")
    private String notes;

    public Entry() {
        this.bookedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Transaction getTransaction() { return transaction; }
    public void setTransaction(Transaction t) { this.transaction = t; }
    public Account getAccount() { return account; }
    public void setAccount(Account account) { this.account = account; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public EntryType getEntryType() { return entryType; }
    public void setEntryType(EntryType entryType) { this.entryType = entryType; }
    public LocalDateTime getChargedAt() { return chargedAt; }
    public void setChargedAt(LocalDateTime chargedAt) { this.chargedAt = chargedAt; }
    public LocalDateTime getBookedAt() { return bookedAt; }
    public ProposedAction getOriginatingAction() { return originatingAction; }
    public void setOriginatingAction(ProposedAction a) { this.originatingAction = a; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
