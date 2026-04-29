package com.rpl.repository;
import com.rpl.domain.AuditLogEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface AuditLogEntryRepository extends JpaRepository<AuditLogEntry, Long> {}
