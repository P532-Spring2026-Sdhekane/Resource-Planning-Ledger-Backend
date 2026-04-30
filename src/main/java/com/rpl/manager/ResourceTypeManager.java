package com.rpl.manager;

import com.rpl.domain.Account;
import com.rpl.domain.PostingRule;
import com.rpl.domain.ResourceType;
import com.rpl.repository.AccountRepository;
import com.rpl.repository.PostingRuleRepository;
import com.rpl.repository.ResourceTypeRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * Manager layer: manages ResourceType catalogue and its associated pool/alert accounts.
 */
@Service
@Transactional
public class ResourceTypeManager {

    private final ResourceTypeRepository resourceTypeRepository;
    private final AccountRepository accountRepository;
    private final PostingRuleRepository postingRuleRepository;

    public ResourceTypeManager(ResourceTypeRepository resourceTypeRepository,
                               AccountRepository accountRepository,
                               PostingRuleRepository postingRuleRepository) {
        this.resourceTypeRepository = resourceTypeRepository;
        this.accountRepository = accountRepository;
        this.postingRuleRepository = postingRuleRepository;
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

        // Save RT first so accounts can reference it
        resourceTypeRepository.save(rt);

        // Create linked pool and alert accounts
        Account pool  = accountRepository.save(new Account(name + " Pool",  Account.Kind.POOL,       rt));
        Account alert = accountRepository.save(new Account(name + " Alert", Account.Kind.ALERT_MEMO, rt));

        rt.setPoolAccount(pool);
        rt.setAlertAccount(alert);
        resourceTypeRepository.save(rt);

        // Wire up the posting rule: when pool goes negative → fire alert to alert account
        PostingRule rule = new PostingRule(pool, alert, "OVER_CONSUMPTION_ALERT");
        postingRuleRepository.save(rule);

        return rt;
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