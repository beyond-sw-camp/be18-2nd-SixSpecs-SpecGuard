package com.beyond.specguard.evaluationprofile.model.service;

import com.beyond.specguard.evaluationprofile.model.dto.command.CreateEvaluationWeightCommand;
import com.beyond.specguard.evaluationprofile.model.entity.EvaluationProfile;
import com.beyond.specguard.evaluationprofile.model.entity.EvaluationWeight;

import java.util.List;

public interface EvaluationWeightService {
    List<EvaluationWeight> createWeights(CreateEvaluationWeightCommand createEvaluationWeightCommand);

    void delete(EvaluationProfile profile);
}
