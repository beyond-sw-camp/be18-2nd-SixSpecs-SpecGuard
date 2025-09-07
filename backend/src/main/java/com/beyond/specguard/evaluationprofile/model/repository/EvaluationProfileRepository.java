package com.beyond.specguard.evaluationprofile.model.repository;

import com.beyond.specguard.evaluationprofile.model.entity.EvaluationProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EvaluationProfileRepository extends JpaRepository<EvaluationProfile, UUID> {
}
