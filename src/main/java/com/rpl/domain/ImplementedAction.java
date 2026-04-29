package com.rpl.domain;

import com.rpl.domain.composite.ActionStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "implemented_actions")
public class ImplementedAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proposed_action_id", nullable = false)
    private ProposedAction proposedAction;

    private LocalDateTime actualStart;
    private String actualParty;
    private String actualLocation;

    @Enumerated(EnumType.STRING)
    private ActionStatus status = ActionStatus.IN_PROGRESS;

    public ImplementedAction() {}

    public ImplementedAction(ProposedAction proposedAction) {
        this.proposedAction = proposedAction;
        this.actualStart = LocalDateTime.now();
        this.actualParty = proposedAction.getParty();
        this.actualLocation = proposedAction.getLocation();
    }

    public Long getId() { return id; }
    public ProposedAction getProposedAction() { return proposedAction; }
    public void setProposedAction(ProposedAction pa) { this.proposedAction = pa; }
    public LocalDateTime getActualStart() { return actualStart; }
    public void setActualStart(LocalDateTime actualStart) { this.actualStart = actualStart; }
    public String getActualParty() { return actualParty; }
    public void setActualParty(String actualParty) { this.actualParty = actualParty; }
    public String getActualLocation() { return actualLocation; }
    public void setActualLocation(String actualLocation) { this.actualLocation = actualLocation; }
    public ActionStatus getStatus() { return status; }
    public void setStatus(ActionStatus status) { this.status = status; }
}
