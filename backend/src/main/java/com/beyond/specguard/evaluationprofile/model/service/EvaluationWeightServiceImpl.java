package com.beyond.specguard.evaluationprofile.model.service;

import com.beyond.specguard.evaluationprofile.model.dto.command.CreateEvaluationWeightCommand;
import com.beyond.specguard.evaluationprofile.model.entity.EvaluationWeight;
import com.beyond.specguard.evaluationprofile.model.respository.EvaluationWeightRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EvaluationWeightServiceImpl implements EvaluationWeightService {

    private final EvaluationWeightRepository evaluationWeightRepository;

    @Override
    public List<EvaluationWeight> createWeights(CreateEvaluationWeightCommand command) {
        return evaluationWeightRepository.saveAll(
                command.weights()
                        .stream()
                        .map(w -> w.toEntity(command.evaluationProfile())).toList()
        );
    }
}
