package com.beyond.specguard.evaluationprofile.model.repository;

import com.beyond.specguard.company.common.model.entity.ClientCompany;
import com.beyond.specguard.evaluationprofile.model.entity.EvaluationProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EvaluationProfileRepository extends JpaRepository<EvaluationProfile, UUID> {
    Page<EvaluationProfile> findByCompanyAndIsActive(ClientCompany company, Boolean active, Pageable pageable);

    Page<EvaluationProfile> findByCompany(ClientCompany company, Pageable pageable);
}
