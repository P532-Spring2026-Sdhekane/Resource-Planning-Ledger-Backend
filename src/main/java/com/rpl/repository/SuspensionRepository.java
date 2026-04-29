package com.rpl.repository;
import com.rpl.domain.Suspension;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface SuspensionRepository extends JpaRepository<Suspension, Long> {
    List<Suspension> findByProposedActionId(Long actionId);
}
