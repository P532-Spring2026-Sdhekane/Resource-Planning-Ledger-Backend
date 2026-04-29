package com.rpl.repository;
import com.rpl.domain.ResourceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface ResourceTypeRepository extends JpaRepository<ResourceType, Long> {}
