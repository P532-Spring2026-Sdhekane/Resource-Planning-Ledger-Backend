package com.rpl.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "resource_types")
public class ResourceType {

    public enum Kind { ASSET, CONSUMABLE }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Kind kind;

    @Column(nullable = false)
    private String unitOfMeasure;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "pool_account_id")
    private Account poolAccount;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "alert_account_id")
    private Account alertAccount;

    private BigDecimal unitCost = BigDecimal.ZERO;

    public ResourceType() {}

    public ResourceType(String name, Kind kind, String unitOfMeasure) {
        this.name = name;
        this.kind = kind;
        this.unitOfMeasure = unitOfMeasure;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Kind getKind() { return kind; }
    public void setKind(Kind kind) { this.kind = kind; }
    public String getUnitOfMeasure() { return unitOfMeasure; }
    public void setUnitOfMeasure(String unitOfMeasure) { this.unitOfMeasure = unitOfMeasure; }
    public Account getPoolAccount() { return poolAccount; }
    public void setPoolAccount(Account poolAccount) { this.poolAccount = poolAccount; }
    public Account getAlertAccount() { return alertAccount; }
    public void setAlertAccount(Account alertAccount) { this.alertAccount = alertAccount; }
    public BigDecimal getUnitCost() { return unitCost; }
    public void setUnitCost(BigDecimal u) { this.unitCost = u; }
}
