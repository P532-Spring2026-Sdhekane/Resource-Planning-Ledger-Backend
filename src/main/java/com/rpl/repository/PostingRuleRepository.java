package com.rpl.repository;
import com.rpl.domain.PostingRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface PostingRuleRepository extends JpaRepository<PostingRule, Long> {
    List<PostingRule> findByTriggerAccountId(Long accountId);
}
