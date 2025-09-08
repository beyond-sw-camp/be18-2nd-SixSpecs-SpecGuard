package com.beyond.specguard.evaluationprofile.controller;

import com.beyond.specguard.auth.model.entity.ClientUser;
import com.beyond.specguard.auth.model.service.CustomUserDetails;
import com.beyond.specguard.evaluationprofile.model.dto.command.CreateEvaluationProfileCommand;
import com.beyond.specguard.evaluationprofile.model.dto.command.GetEvaluationProfileCommand;
import com.beyond.specguard.evaluationprofile.model.dto.command.SearchEvaluationProfileCommand;
import com.beyond.specguard.evaluationprofile.model.dto.request.EvaluationProfileRequestDto;
import com.beyond.specguard.evaluationprofile.model.dto.response.EvaluationProfileListResponseDto;
import com.beyond.specguard.evaluationprofile.model.dto.response.EvaluationProfileResponseDto;
import com.beyond.specguard.evaluationprofile.model.service.EvaluationProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/evaluationProfiles")
@RequiredArgsConstructor
public class EvaluationProfileController {

    private final EvaluationProfileService evaluationProfileService;

    @PostMapping("/")
    public ResponseEntity<EvaluationProfileResponseDto> createProfile(
            @Valid @RequestBody EvaluationProfileRequestDto request,
            Authentication authentication
    ) {
        ClientUser user =  getUserDetails(authentication).getUser();
        EvaluationProfileResponseDto responseDto = evaluationProfileService.createProfile(new CreateEvaluationProfileCommand(user, request));

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(responseDto);
    }


    private CustomUserDetails getUserDetails(Authentication authentication) {
        // company 추출
        return (CustomUserDetails) authentication.getPrincipal();
    }

    @GetMapping("/{profileId}")
    public ResponseEntity<EvaluationProfileResponseDto> getProfile(
            @PathVariable UUID profileId,
            Authentication authentication
    ) {
        ClientUser user = getUserDetails(authentication).getUser();

        EvaluationProfileResponseDto responseDto = evaluationProfileService.getProfile(new GetEvaluationProfileCommand(profileId, user));

        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/")
    public ResponseEntity<EvaluationProfileListResponseDto> getProfiles(
            @RequestParam(required = false) Boolean isActive,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            Authentication authentication
    ) {
        ClientUser user = getUserDetails(authentication).getUser();

        EvaluationProfileListResponseDto profilesResponseDto = evaluationProfileService.getProfiles(
                new SearchEvaluationProfileCommand(user, isActive, pageable)
        );

        return ResponseEntity.ok(profilesResponseDto);
    }

    @DeleteMapping("/{profileId}")
    public ResponseEntity<Void> deleteProfile(
            @PathVariable UUID profileId,
            Authentication authentication
    ) {
        ClientUser user = getUserDetails(authentication).getUser();

        evaluationProfileService.deleteProfile(user, profileId);

        return ResponseEntity.noContent().build();
    }
}
