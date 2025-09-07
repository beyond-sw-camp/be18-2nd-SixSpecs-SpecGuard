package com.beyond.specguard.evaluationprofile.model.service;

import com.beyond.specguard.evaluationprofile.model.dto.command.CreateEvaluationProfileCommand;
import com.beyond.specguard.evaluationprofile.model.dto.command.CreateEvaluationWeightCommand;
import com.beyond.specguard.evaluationprofile.model.entity.EvaluationProfile;
import com.beyond.specguard.evaluationprofile.model.entity.EvaluationWeight;
import com.beyond.specguard.evaluationprofile.model.repository.EvaluationProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EvaluationProfileServiceImpl implements EvaluationProfileService {

    private final EvaluationProfileRepository evaluationProfileRepository;
    private final EvaluationWeightService evaluationWeightService;
    @Override
    public EvaluationProfile createProfile(CreateEvaluationProfileCommand command) {
        // EvaluationProfile 생성
        EvaluationProfile result = evaluationProfileRepository.save(command.evaluationProfileRequestDto().toEntity(command.company()));

        // EvaluationWeight 들 생성
        List<EvaluationWeight> weights = evaluationWeightService.createWeights(
                new CreateEvaluationWeightCommand(result, command.evaluationProfileRequestDto().getWeights()));

        return result;
    }
}
