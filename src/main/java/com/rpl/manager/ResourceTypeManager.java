package com.rpl.manager;

import com.rpl.domain.Account;
import com.rpl.domain.ResourceType;
import com.rpl.repository.AccountRepository;
import com.rpl.repository.ResourceTypeRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.util.List;


@Service
@Transactional
public class ResourceTypeManager {

    private final ResourceTypeRepository resourceTypeRepository;
    private final AccountRepository accountRepository;

    public ResourceTypeManager(ResourceTypeRepository resourceTypeRepository,
                               AccountRepository accountRepository) {
        this.resourceTypeRepository = resourceTypeRepository;
        this.accountRepository = accountRepository;
    }

    public List<ResourceType> listAll() {
        return resourceTypeRepository.findAll();
    }

    public ResourceType findById(Long id) {
        return resourceTypeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("ResourceType not found: " + id));
    }

    public ResourceType create(String name, ResourceType.Kind kind, String unitOfMeasure) {
        ResourceType rt = new ResourceType(name, kind, unitOfMeasure);

       
        Account pool = new Account(name + " Pool", Account.Kind.POOL, rt);
        Account alert = new Account(name + " Alert", Account.Kind.ALERT_MEMO, rt);

        rt.setPoolAccount(pool);
        rt.setAlertAccount(alert);

        return resourceTypeRepository.save(rt);
    }

    public ResourceType update(Long id, String name, ResourceType.Kind kind, String unit) {
        ResourceType rt = findById(id);
        rt.setName(name);
        rt.setKind(kind);
        rt.setUnitOfMeasure(unit);
        return resourceTypeRepository.save(rt);
    }

    public void delete(Long id) {
        resourceTypeRepository.deleteById(id);
    }
}
