package com.beyond.specguard.evaluationprofile.model.dto.command;


import com.beyond.specguard.company.common.model.entity.ClientUser;
import org.springframework.data.domain.Pageable;

public record SearchEvaluationProfileCommand(
        ClientUser user,
        Boolean isActive,
        Pageable pageable) {
}
