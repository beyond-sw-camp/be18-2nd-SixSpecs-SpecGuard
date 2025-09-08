package com.beyond.specguard.evaluationprofile.model.service;

import com.beyond.specguard.auth.model.entity.ClientUser;
import com.beyond.specguard.common.exception.CustomException;
import com.beyond.specguard.common.exception.errorcode.CommonErrorCode;
import com.beyond.specguard.evaluationprofile.exception.errorcode.EvaluationProfileErrorCode;
import com.beyond.specguard.evaluationprofile.model.dto.command.CreateEvaluationProfileCommand;
import com.beyond.specguard.evaluationprofile.model.dto.command.CreateEvaluationWeightCommand;
import com.beyond.specguard.evaluationprofile.model.dto.command.GetEvaluationProfileCommand;
import com.beyond.specguard.evaluationprofile.model.dto.command.SearchEvaluationProfileCommand;
import com.beyond.specguard.evaluationprofile.model.dto.command.UpdateEvaluationProfileCommand;
import com.beyond.specguard.evaluationprofile.model.dto.response.EvaluationProfileListResponseDto;
import com.beyond.specguard.evaluationprofile.model.dto.response.EvaluationProfileResponseDto;
import com.beyond.specguard.evaluationprofile.model.entity.EvaluationProfile;
import com.beyond.specguard.evaluationprofile.model.entity.EvaluationWeight;
import com.beyond.specguard.evaluationprofile.model.repository.EvaluationProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EvaluationProfileServiceImpl implements EvaluationProfileService {

    private final EvaluationProfileRepository evaluationProfileRepository;
    private final EvaluationWeightService evaluationWeightService;

    @Override
    @Transactional
    public EvaluationProfileResponseDto createProfile(CreateEvaluationProfileCommand command) {
        // 권한 OWNER, MANAGER 체크
        if (hasNoWriteRole(command.user().getRole())) {
            throw new CustomException(CommonErrorCode.ACCESS_DENIED);
        }

        // EvaluationProfile 생성
        EvaluationProfile profile = evaluationProfileRepository.save(command.evaluationProfileRequestDto().toEntity(command.user().getCompany()));

        // EvaluationWeight 들 생성
        List<EvaluationWeight> weights = evaluationWeightService.createWeights(
                new CreateEvaluationWeightCommand(profile, command.evaluationProfileRequestDto().getWeights()));


        weights.forEach(profile::addWeight);

        return EvaluationProfileResponseDto.fromEntity(profile);
    }

    @Override
    @Transactional(readOnly = true)
    public EvaluationProfileResponseDto getProfile(GetEvaluationProfileCommand command) {

        // 1. EvaluationProfile 조회 (ID 기준)
        EvaluationProfile profile = evaluationProfileRepository.findById(command.profileId())
                .orElseThrow(() -> new CustomException(EvaluationProfileErrorCode.EVALUATION_PROFILE_NOT_FOUND));

        // 2. 회사 소속 확인
        if (!profile.getCompany().getId().equals(command.user().getCompany().getId())) {
            throw new CustomException(EvaluationProfileErrorCode.ACCESS_DENIED);
        }

        return EvaluationProfileResponseDto.fromEntity(profile);
    }

    @Override
    @Transactional(readOnly = true)
    public EvaluationProfileListResponseDto getProfiles(SearchEvaluationProfileCommand command) {
        // 조회
        Page<EvaluationProfileResponseDto> profilePage = ((command.isActive() != null) ?
                evaluationProfileRepository.findByCompanyAndIsActive(command.user().getCompany(), command.isActive(), command.pageable()) :
                evaluationProfileRepository.findByCompany(command.user().getCompany(), command.pageable()))
                .map(EvaluationProfileResponseDto::fromEntity);

        long totalElements = evaluationProfileRepository.count();

        // DTO 변환
        return EvaluationProfileListResponseDto.builder()
                .evaluationProfiles(profilePage.getContent())
                .totalElements(totalElements)
                .totalPages(profilePage.getTotalPages())
                .pageNumber(profilePage.getNumber())
                .pageSize(profilePage.getSize())
                .build();
    }

    @Override
    @Transactional
    public void deleteProfile(ClientUser user, UUID profileId) {
        // 1. 조회
        EvaluationProfile profile = evaluationProfileRepository.findById(profileId)
                .orElseThrow(() -> new CustomException(EvaluationProfileErrorCode.EVALUATION_PROFILE_NOT_FOUND));

        // 2. 권한 체크
        if (hasNoWriteRole(user.getRole())) {
            throw new CustomException(CommonErrorCode.ACCESS_DENIED);
        }

        // 3. 회사 일치 체크
        if (!profile.getCompany().getId().equals(user.getCompany().getId())) {
            throw new CustomException(CommonErrorCode.ACCESS_DENIED);
        }

        // 4. 삭제 (하위 항목들도 삭제 필요)
        evaluationProfileRepository.delete(profile);
        evaluationWeightService.delete(profile);
    }

    @Override
    @Transactional
    public EvaluationProfileResponseDto updateProfile(UpdateEvaluationProfileCommand command) {
        // 1. 프로필 조회
        EvaluationProfile profile = evaluationProfileRepository.findById(command.profileId())
                .orElseThrow(() -> new CustomException(EvaluationProfileErrorCode.EVALUATION_PROFILE_NOT_FOUND));

        // 2. 회사 일치 체크
        if (!profile.getCompany().getId().equals(command.user().getCompany().getId())) {
            throw new CustomException(CommonErrorCode.ACCESS_DENIED);
        }

        // 3. 권한 체크
        if (hasNoWriteRole(command.user().getRole())) {
            throw new CustomException(CommonErrorCode.ACCESS_DENIED);
        }

        // 4. 프로필 기본 정보 업데이트
        profile.update(command.request());

        // 5. weights 업데이트 (전체 교체)
        if (command.request().getWeights() != null) {
            // 기존 weights 제거
            profile.getWeights().clear();

            // 새로운 weights 생성 후 추가
            List<EvaluationWeight> newWeights = evaluationWeightService.createWeights(
                    new CreateEvaluationWeightCommand(profile, command.request().getWeights())
            );
            profile.getWeights().addAll(newWeights);
        }

        return EvaluationProfileResponseDto.fromEntity(evaluationProfileRepository.save(profile));
    }

    private boolean hasNoWriteRole(ClientUser.Role role) {
        return !EnumSet.of(ClientUser.Role.OWNER, ClientUser.Role.MANAGER).contains(role);
    }
}
