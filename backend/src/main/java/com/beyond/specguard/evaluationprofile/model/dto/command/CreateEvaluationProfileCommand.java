package com.beyond.specguard.evaluationprofile.model.dto.command;

import com.beyond.specguard.auth.model.entity.ClientUser;
import com.beyond.specguard.evaluationprofile.model.dto.request.EvaluationProfileRequestDto;

public record CreateEvaluationProfileCommand(
        ClientUser user,
        EvaluationProfileRequestDto evaluationProfileRequestDto
) {
}
