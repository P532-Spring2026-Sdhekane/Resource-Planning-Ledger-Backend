package com.rpl.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "protocol_steps")
public class ProtocolStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "protocol_id", nullable = false)
    private Protocol protocol;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_protocol_id")
    private Protocol subProtocol;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "protocol_step_deps", joinColumns = @JoinColumn(name = "step_id"))
    @Column(name = "depends_on_step_name")
    private List<String> dependsOn = new ArrayList<>();

    public ProtocolStep() {}
    public ProtocolStep(String name) { this.name = name; }

    public Long getId() { return id; }
    @JsonIgnore
    public Protocol getProtocol() { return protocol; }
    public void setProtocol(Protocol protocol) { this.protocol = protocol; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Protocol getSubProtocol() { return subProtocol; }
    public void setSubProtocol(Protocol subProtocol) { this.subProtocol = subProtocol; }
    public List<String> getDependsOn() { return dependsOn; }
    public void setDependsOn(List<String> dependsOn) { this.dependsOn = dependsOn; }
}