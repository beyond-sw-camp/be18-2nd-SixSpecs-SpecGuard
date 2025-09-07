package com.beyond.specguard.evaluationprofile.controller;

import com.beyond.specguard.auth.model.entity.ClientCompany;
import com.beyond.specguard.auth.model.service.CustomUserDetails;
import com.beyond.specguard.evaluationprofile.model.dto.command.CreateEvaluationProfileCommand;
import com.beyond.specguard.evaluationprofile.model.dto.request.EvaluationProfileRequestDto;
import com.beyond.specguard.evaluationprofile.model.dto.response.EvaluationProfileResponseDto;
import com.beyond.specguard.evaluationprofile.model.entity.EvaluationProfile;
import com.beyond.specguard.evaluationprofile.model.service.EvaluationProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/company")
@RequiredArgsConstructor
public class EvaluationProfileController {

    private final EvaluationProfileService evaluationProfileService;

    @PostMapping("/evaluationProfile")
    public ResponseEntity<EvaluationProfileResponseDto> createProfile(
            @Valid @RequestBody EvaluationProfileRequestDto request,
            Authentication authentication
    ) {
        // company 추출
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        ClientCompany company = customUserDetails.getUser().getCompany();

        EvaluationProfile evaluationProfile = evaluationProfileService.createProfile(new CreateEvaluationProfileCommand(company, request));

        return ResponseEntity.ok(EvaluationProfileResponseDto.fromEntity(evaluationProfile));
    }


}
