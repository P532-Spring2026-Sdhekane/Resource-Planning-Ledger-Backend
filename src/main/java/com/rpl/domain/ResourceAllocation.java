package com.rpl.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "resource_allocations")
public class ResourceAllocation {

    public enum AllocationKind { GENERAL, SPECIFIC }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "action_id", nullable = false)
    private ProposedAction action;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "resource_type_id", nullable = false)
    private ResourceType resourceType;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AllocationKind kind = AllocationKind.GENERAL;

    private String assetId;      
    private LocalDate periodStart;
    private LocalDate periodEnd;

    public ResourceAllocation() {}

    public ResourceAllocation(ResourceType resourceType, BigDecimal quantity, AllocationKind kind) {
        this.resourceType = resourceType;
        this.quantity = quantity;
        this.kind = kind;
    }

    public Long getId() { return id; }
    public ProposedAction getAction() { return action; }
    public void setAction(ProposedAction action) { this.action = action; }
    public ResourceType getResourceType() { return resourceType; }
    public void setResourceType(ResourceType rt) { this.resourceType = rt; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public AllocationKind getKind() { return kind; }
    public void setKind(AllocationKind kind) { this.kind = kind; }
    public String getAssetId() { return assetId; }
    public void setAssetId(String assetId) { this.assetId = assetId; }
    public LocalDate getPeriodStart() { return periodStart; }
    public void setPeriodStart(LocalDate periodStart) { this.periodStart = periodStart; }
    public LocalDate getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(LocalDate periodEnd) { this.periodEnd = periodEnd; }
}
