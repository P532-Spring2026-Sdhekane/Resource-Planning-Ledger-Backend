package com.rpl.repository;
import com.rpl.domain.ResourceAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface ResourceAllocationRepository extends JpaRepository<ResourceAllocation, Long> {}
