package com.rpl.repository;
import com.rpl.domain.ProposedAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface ProposedActionRepository extends JpaRepository<ProposedAction, Long> {}
