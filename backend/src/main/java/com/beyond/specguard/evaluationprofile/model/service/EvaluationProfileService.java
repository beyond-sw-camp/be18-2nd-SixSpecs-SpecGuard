package com.beyond.specguard.evaluationprofile.model.service;

import com.beyond.specguard.auth.model.entity.ClientUser;
import com.beyond.specguard.evaluationprofile.model.dto.command.CreateEvaluationProfileCommand;
import com.beyond.specguard.evaluationprofile.model.dto.command.GetEvaluationProfileCommand;
import com.beyond.specguard.evaluationprofile.model.dto.command.SearchEvaluationProfileCommand;
import com.beyond.specguard.evaluationprofile.model.dto.response.EvaluationProfileListResponseDto;
import com.beyond.specguard.evaluationprofile.model.dto.response.EvaluationProfileResponseDto;

import java.util.UUID;

public interface EvaluationProfileService {

    EvaluationProfileResponseDto createProfile(CreateEvaluationProfileCommand createEvaluationProfileCommand);

    EvaluationProfileResponseDto getProfile(GetEvaluationProfileCommand getEvaluationProfileCommand);

    EvaluationProfileListResponseDto getProfiles(SearchEvaluationProfileCommand searchEvaluationProfileCommand);

    void deleteProfile(ClientUser user, UUID profileId);
}
