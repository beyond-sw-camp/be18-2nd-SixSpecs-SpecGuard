package com.beyond.specguard.evaluationprofile.model.dto.command;



import com.beyond.specguard.company.common.model.entity.ClientUser;

import java.util.UUID;

public record GetEvaluationProfileCommand(
        UUID profileId,
        ClientUser user) {
}
