package com.beyond.specguard.evaluationprofile.model.service;

import com.beyond.specguard.evaluationprofile.model.dto.command.CreateEvaluationWeightCommand;
import com.beyond.specguard.evaluationprofile.model.entity.EvaluationProfile;
import com.beyond.specguard.evaluationprofile.model.entity.EvaluationWeight;
import com.beyond.specguard.evaluationprofile.model.repository.EvaluationWeightRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EvaluationWeightServiceImpl implements EvaluationWeightService {

    private final EvaluationWeightRepository evaluationWeightRepository;

    @Override
    @Transactional
    public List<EvaluationWeight> createWeights(CreateEvaluationWeightCommand command) {
        return evaluationWeightRepository.saveAll(
                command.weights()
                        .stream()
                        .map(w -> w.fromEntity(command.evaluationProfile())).toList()
        );
    }

    @Override
    @Transactional
    public void delete(EvaluationProfile profile) {
        evaluationWeightRepository.deleteByProfile(profile);
    }
}
