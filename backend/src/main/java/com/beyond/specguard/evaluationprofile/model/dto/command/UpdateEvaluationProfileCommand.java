package com.beyond.specguard.evaluationprofile.model.dto.command;

import com.beyond.specguard.auth.model.entity.ClientUser;
import com.beyond.specguard.evaluationprofile.model.dto.request.EvaluationProfileRequestDto;

import java.util.UUID;

public record UpdateEvaluationProfileCommand(
        ClientUser user,
        UUID profileId,
        EvaluationProfileRequestDto request) {
}
