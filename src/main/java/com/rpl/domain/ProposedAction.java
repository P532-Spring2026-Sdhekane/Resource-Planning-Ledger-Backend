package com.rpl.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rpl.domain.composite.ActionStatus;
import com.rpl.domain.composite.PlanNode;
import com.rpl.domain.composite.PlanNodeVisitor;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Leaf node in the Composite tree.
 * Holds mutable status managed by the State pattern (ActionStateMachine).
 * Status is persisted as a string; the state object is resolved at runtime.
 */
@Entity
@Table(name = "proposed_actions")
public class ProposedAction implements PlanNode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id")
    private Plan plan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "protocol_id")
    private Protocol protocol;

    private String party;
    private LocalDate timeRef;
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActionStatus statusEnum = ActionStatus.PROPOSED;

    @OneToMany(mappedBy = "action", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ResourceAllocation> allocations = new ArrayList<>();

    @OneToOne(mappedBy = "proposedAction", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private ImplementedAction implementedAction;

    @OneToMany(mappedBy = "proposedAction", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Suspension> suspensions = new ArrayList<>();

    // Dependency references within the plan (stored as action names)
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "action_dependencies", joinColumns = @JoinColumn(name = "action_id"))
    @Column(name = "depends_on_name")
    private List<String> dependsOnNames = new ArrayList<>();

    public ProposedAction() {}

    public ProposedAction(String name) {
        this.name = name;
    }

    // --- PlanNode interface ---

    @Override
    public Long getId() { return id; }

    @Override
    public String getName() { return name; }

    @Override
    public String getNodeType() { return "LEAF"; }

    @Override
    public ActionStatus getStatus() { return statusEnum; }

    @Override
    public BigDecimal getTotalAllocatedQuantity(ResourceType resourceType) {
        return allocations.stream()
            .filter(a -> a.getResourceType().getId().equals(resourceType.getId()))
            .map(ResourceAllocation::getQuantity)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public void accept(PlanNodeVisitor visitor) {
        visitor.visitLeaf(this);
    }

    // --- Getters / setters ---

    public void setName(String name) { this.name = name; }
    @JsonIgnore
    @JsonIgnore
    public Plan getPlan() { return plan; }
    public void setPlan(Plan plan) { this.plan = plan; }
    public Protocol getProtocol() { return protocol; }
    public void setProtocol(Protocol protocol) { this.protocol = protocol; }
    public String getParty() { return party; }
    public void setParty(String party) { this.party = party; }
    public LocalDate getTimeRef() { return timeRef; }
    public void setTimeRef(LocalDate timeRef) { this.timeRef = timeRef; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public ActionStatus getStatusEnum() { return statusEnum; }
    public void setStatusEnum(ActionStatus statusEnum) { this.statusEnum = statusEnum; }
    public List<ResourceAllocation> getAllocations() { return allocations; }
    public void addAllocation(ResourceAllocation a) { a.setAction(this); allocations.add(a); }
    public ImplementedAction getImplementedAction() { return implementedAction; }
    public void setImplementedAction(ImplementedAction ia) { this.implementedAction = ia; }
    public List<Suspension> getSuspensions() { return suspensions; }
    public List<String> getDependsOnNames() { return dependsOnNames; }
    public void setDependsOnNames(List<String> d) { this.dependsOnNames = d; }
}
