package com.beyond.specguard.companytemplate.model.repository;

import com.beyond.specguard.companytemplate.model.entity.CompanyTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface CompanyTemplateRepository extends JpaRepository<CompanyTemplate, UUID>, JpaSpecificationExecutor<CompanyTemplate> {
  @Query("""
        select t.id
        from CompanyTemplate t
        where t.endDate is not null
          and t.endDate < :now
    """)
    List<UUID> findExpiredTemplateIds(LocalDateTime now);

  List<CompanyTemplate> findAllByClientCompany_Slug(String companySlug);
}
