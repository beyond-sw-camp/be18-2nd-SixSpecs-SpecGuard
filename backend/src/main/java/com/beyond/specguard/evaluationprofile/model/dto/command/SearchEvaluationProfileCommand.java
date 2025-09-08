package com.beyond.specguard.evaluationprofile.model.dto.command;

public record SearchEvaluationProfileCommand(
        com.beyond.specguard.auth.model.entity.ClientUser user,
        Boolean isActive,
        org.springframework.data.domain.Pageable pageable) {
}
