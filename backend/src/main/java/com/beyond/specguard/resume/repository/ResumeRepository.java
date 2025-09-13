package com.beyond.specguard.resume.repository;

import com.beyond.specguard.resume.entity.core.Resume;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ResumeRepository extends JpaRepository<Resume, UUID> {
    boolean existsByEmail(String email);
    @Query("""
        select r.id
        from Resume r
        where r.templateId in :templateIds
          and not exists (
            select 1 from CompanyFormSubmission s
            where s.resume = r
          )
    """)
    List<UUID> findUnsubmittedIdsByTemplateIds(List<UUID> templateIds, Pageable pageable);
}
