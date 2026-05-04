package com.rpl.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "suspensions")
public class Suspension {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proposed_action_id", nullable = false)
    private ProposedAction proposedAction;

    @Column(columnDefinition = "TEXT")
    private String reason;

    private LocalDate startDate;
    private LocalDate endDate;

    public Suspension() {}

    public Suspension(ProposedAction action, String reason) {
        this.proposedAction = action;
        this.reason = reason;
        this.startDate = LocalDate.now();
    }

    public Long getId() { return id; }
    @JsonIgnore
    public ProposedAction getProposedAction() { return proposedAction; }
    public String getReason() { return reason; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
}
