package com.rpl.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "posting_rules")
public class PostingRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "trigger_account_id", nullable = false)
    private Account triggerAccount;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "output_account_id", nullable = false)
    private Account outputAccount;

    private String strategyType;  // e.g., "OVER_CONSUMPTION_ALERT"

    public PostingRule() {}

    public PostingRule(Account triggerAccount, Account outputAccount, String strategyType) {
        this.triggerAccount = triggerAccount;
        this.outputAccount = outputAccount;
        this.strategyType = strategyType;
    }

    public Long getId() { return id; }
    public Account getTriggerAccount() { return triggerAccount; }
    public Account getOutputAccount() { return outputAccount; }
    public String getStrategyType() { return strategyType; }
}
