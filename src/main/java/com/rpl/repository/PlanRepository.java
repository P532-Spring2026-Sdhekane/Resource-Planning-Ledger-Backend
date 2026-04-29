package com.rpl.repository;
import com.rpl.domain.Plan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface PlanRepository extends JpaRepository<Plan, Long> {}
