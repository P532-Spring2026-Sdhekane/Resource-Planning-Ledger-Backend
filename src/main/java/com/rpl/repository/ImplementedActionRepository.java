package com.rpl.repository;
import com.rpl.domain.ImplementedAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface ImplementedActionRepository extends JpaRepository<ImplementedAction, Long> {}
