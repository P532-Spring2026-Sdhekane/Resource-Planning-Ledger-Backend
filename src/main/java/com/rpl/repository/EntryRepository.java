package com.rpl.repository;
import com.rpl.domain.Entry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface EntryRepository extends JpaRepository<Entry, Long> {
    List<Entry> findByAccountId(Long accountId);
}
