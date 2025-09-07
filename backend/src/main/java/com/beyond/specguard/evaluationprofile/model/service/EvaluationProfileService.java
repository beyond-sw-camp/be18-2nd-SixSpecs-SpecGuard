package com.beyond.specguard.evaluationprofile.model.service;

import com.beyond.specguard.evaluationprofile.model.dto.command.CreateEvaluationProfileCommand;
import com.beyond.specguard.evaluationprofile.model.entity.EvaluationProfile;

public interface EvaluationProfileService {

    EvaluationProfile createProfile(CreateEvaluationProfileCommand createEvaluationProfileCommand);
}
