package com.beyond.specguard.evaluationprofile.model.dto.command;

import com.beyond.specguard.auth.model.entity.ClientCompany;
import com.beyond.specguard.evaluationprofile.model.dto.request.EvaluationProfileRequestDto;

public record CreateEvaluationProfileCommand(
        ClientCompany company,
        EvaluationProfileRequestDto evaluationProfileRequestDto
) {
}
