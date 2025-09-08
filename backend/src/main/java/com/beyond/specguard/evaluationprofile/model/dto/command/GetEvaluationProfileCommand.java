package com.beyond.specguard.evaluationprofile.model.dto.command;

import java.util.UUID;

public record GetEvaluationProfileCommand(UUID profileId, com.beyond.specguard.auth.model.entity.ClientUser user) {
}
