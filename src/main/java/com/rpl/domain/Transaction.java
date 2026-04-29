package com.rpl.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String description;

    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Entry> entries = new ArrayList<>();

    public Transaction() {
        this.createdAt = LocalDateTime.now();
    }

    public Transaction(String description) {
        this();
        this.description = description;
    }

    public Long getId() { return id; }
    public String getDescription() { return description; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<Entry> getEntries() { return entries; }
    public void addEntry(Entry e) { e.setTransaction(this); entries.add(e); }
}
