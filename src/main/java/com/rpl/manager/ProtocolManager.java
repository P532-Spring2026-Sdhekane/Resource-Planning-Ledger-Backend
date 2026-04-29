package com.rpl.manager;

import com.rpl.domain.Protocol;
import com.rpl.domain.ProtocolStep;
import com.rpl.repository.ProtocolRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.util.List;


@Service
@Transactional
public class ProtocolManager {

    private final ProtocolRepository protocolRepository;

    public ProtocolManager(ProtocolRepository protocolRepository) {
        this.protocolRepository = protocolRepository;
    }

    public List<Protocol> listAll() {
        return protocolRepository.findAll();
    }

    public Protocol findById(Long id) {
        return protocolRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Protocol not found: " + id));
    }

    public Protocol create(String name, String description) {
        Protocol p = new Protocol(name, description);
        return protocolRepository.save(p);
    }

    public Protocol update(Long id, String name, String description) {
        Protocol p = findById(id);
        p.setName(name);
        p.setDescription(description);
        return protocolRepository.save(p);
    }

    public void delete(Long id) {
        protocolRepository.deleteById(id);
    }

    public Protocol addStep(Long protocolId, String stepName, Long subProtocolId, List<String> dependsOn) {
        Protocol protocol = findById(protocolId);
        ProtocolStep step = new ProtocolStep(stepName);
        if (subProtocolId != null) {
            Protocol sub = findById(subProtocolId);
            step.setSubProtocol(sub);
        }
        if (dependsOn != null) step.setDependsOn(dependsOn);
        protocol.addStep(step);
        return protocolRepository.save(protocol);
    }
}
