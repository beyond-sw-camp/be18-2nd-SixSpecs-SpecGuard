package com.beyond.specguard.evaluationprofile.controller;

import com.beyond.specguard.company.common.model.entity.ClientUser;
import com.beyond.specguard.company.common.model.service.CustomUserDetails;
import com.beyond.specguard.evaluationprofile.model.dto.command.CreateEvaluationProfileCommand;
import com.beyond.specguard.evaluationprofile.model.dto.command.GetEvaluationProfileCommand;
import com.beyond.specguard.evaluationprofile.model.dto.command.SearchEvaluationProfileCommand;
import com.beyond.specguard.evaluationprofile.model.dto.command.UpdateEvaluationProfileCommand;
import com.beyond.specguard.evaluationprofile.model.dto.request.EvaluationProfileRequestDto;
import com.beyond.specguard.evaluationprofile.model.dto.response.EvaluationProfileListResponseDto;
import com.beyond.specguard.evaluationprofile.model.dto.response.EvaluationProfileResponseDto;
import com.beyond.specguard.evaluationprofile.model.service.EvaluationProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/evaluationProfiles")
@RequiredArgsConstructor
@Tag(name = "가중치 프로필 관련 API", description = "가중치 프로필과 관련된 API를 정의합니다.")
public class EvaluationProfileController {

    private final EvaluationProfileService evaluationProfileService;

    // Authentication 에서 UserDetails 추출 메소드
    // TODO: 한 클래스에 둬서 메소드 불러와 사용하기 예) AuthUtil
    private CustomUserDetails getUserDetails(Authentication authentication) {
        return (CustomUserDetails) authentication.getPrincipal();
    }

    @Operation(summary = "가중치 프로필과 가중치 생성", description = "기업 유저 또는 어드민이 새로운 평가 프로필을 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "프로필 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 값"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PostMapping("/")
    public ResponseEntity<EvaluationProfileResponseDto> createProfile(
            @Valid @RequestBody EvaluationProfileRequestDto request,
            Authentication authentication
    ) {
        ClientUser user = getUserDetails(authentication).getUser();
        EvaluationProfileResponseDto responseDto =
                evaluationProfileService.createProfile(new CreateEvaluationProfileCommand(user, request));

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }


    @Operation(summary = "평가 프로필 단건 조회", description = "특정 평가 프로필을 조회합니다. 기업 유저 권한 필요.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음"),
            @ApiResponse(responseCode = "404", description = "프로필을 찾을 수 없음")
    })
    @GetMapping("/{profileId}")
    public ResponseEntity<EvaluationProfileResponseDto> getProfile(
            @Parameter(description = "조회할 프로필 ID", required = true)
            @PathVariable UUID profileId,
            Authentication authentication
    ) {
        ClientUser user = getUserDetails(authentication).getUser();
        EvaluationProfileResponseDto responseDto =
                evaluationProfileService.getProfile(new GetEvaluationProfileCommand(profileId, user));

        return ResponseEntity.ok(responseDto);
    }

    @Operation(summary = "평가 프로필 목록 조회", description = "해당 기업의 평가 프로필 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음")
    })
    @GetMapping("/")
    public ResponseEntity<EvaluationProfileListResponseDto> getProfiles(
            @Parameter(description = "활성화 여부 필터") @RequestParam(required = false) Boolean isActive,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC)
            @ParameterObject
            Pageable pageable,
            Authentication authentication
    ) {
        ClientUser user = getUserDetails(authentication).getUser();
        EvaluationProfileListResponseDto profilesResponseDto =
                evaluationProfileService.getProfiles(new SearchEvaluationProfileCommand(user, isActive, pageable));

        return ResponseEntity.ok(profilesResponseDto);
    }

    @Operation(summary = "평가 프로필 수정", description = "프로필 이름, 설명 등 정보를 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 값"),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음"),
            @ApiResponse(responseCode = "404", description = "프로필을 찾을 수 없음")
    })
    @PutMapping("/{profileId}")
    public ResponseEntity<EvaluationProfileResponseDto> updateProfile(
            @Parameter(description = "수정할 프로필 ID", required = true)
            @PathVariable UUID profileId,
            @Valid @RequestBody EvaluationProfileRequestDto request,
            Authentication authentication
    ) {
        ClientUser user = getUserDetails(authentication).getUser();
        EvaluationProfileResponseDto updatedProfileDto =
                evaluationProfileService.updateProfile(new UpdateEvaluationProfileCommand(user, profileId, request));

        return ResponseEntity.ok(updatedProfileDto);
    }

    @Operation(summary = "평가 프로필 삭제", description = "평가 프로필을 삭제합니다. (하위 가중치도 함께 제거됨)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음"),
            @ApiResponse(responseCode = "404", description = "프로필을 찾을 수 없음")
    })
    @DeleteMapping("/{profileId}")
    public ResponseEntity<Void> deleteProfile(
            @Parameter(description = "삭제할 프로필 ID", required = true)
            @PathVariable UUID profileId,
            Authentication authentication
    ) {
        ClientUser user = getUserDetails(authentication).getUser();
        evaluationProfileService.deleteProfile(user, profileId);

        return ResponseEntity.noContent().build();
    }


}
