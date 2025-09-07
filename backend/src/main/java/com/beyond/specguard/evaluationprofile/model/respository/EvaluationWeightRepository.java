package com.beyond.specguard.evaluationprofile.model.respository;

import com.beyond.specguard.evaluationprofile.model.entity.EvaluationWeight;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EvaluationWeightRepository extends JpaRepository<EvaluationWeight, UUID> {
}
