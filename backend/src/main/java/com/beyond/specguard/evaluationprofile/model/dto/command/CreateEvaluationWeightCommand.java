package com.beyond.specguard.evaluationprofile.model.dto.command;

import com.beyond.specguard.evaluationprofile.model.dto.request.EvaluationProfileRequestDto;
import com.beyond.specguard.evaluationprofile.model.entity.EvaluationProfile;

import java.util.List;

public record CreateEvaluationWeightCommand(
        EvaluationProfile evaluationProfile,
        List<EvaluationProfileRequestDto.WeightCreateDto> weights)
{}
