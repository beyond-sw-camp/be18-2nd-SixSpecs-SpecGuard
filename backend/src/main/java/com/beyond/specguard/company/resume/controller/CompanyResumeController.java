package com.beyond.specguard.company.resume.controller;

import com.beyond.specguard.common.exception.CustomException;
import com.beyond.specguard.company.common.model.entity.ClientUser;
import com.beyond.specguard.company.common.model.service.CustomUserDetails;
import com.beyond.specguard.event.dto.BaseResponse;
import com.beyond.specguard.resume.exception.errorcode.ResumeErrorCode;
import com.beyond.specguard.resume.model.dto.response.ResumeListResponseDto;
import com.beyond.specguard.resume.model.dto.response.ResumeGitSummaryResponse;
import com.beyond.specguard.resume.model.entity.Resume;
import com.beyond.specguard.resume.model.service.ResumeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/company/resumes")
public class CompanyResumeController {

    private final ResumeService resumeService;

    //지원서 목록 조회
    @Operation(
            summary = "지원서 목록 조회",
            description = "기업이 지원서 목록을 조회한다."
    )
    @GetMapping("/list")
    public ResumeListResponseDto list(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestParam(required = false) UUID templateId,
            @RequestParam(required = false) Resume.ResumeStatus status,
            @ParameterObject @Parameter(description = "페이지 정보") @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        if (pageable.getPageSize() > 100) {
            throw new CustomException(ResumeErrorCode.INVALID_PARAMETER);
        }
        ClientUser clientUser = customUserDetails.getUser();
        return resumeService.list(templateId, pageable, clientUser, status);
    }

    //지원서 단건 조회
    @Operation(
            summary = "지원서 단건 조회",
            description = "특정 지원서(resume.id) 단건 조회."
    )
    @GetMapping("/{resumeId}")
    public BaseResponse<ResumeGitSummaryResponse> get(
            @PathVariable UUID resumeId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        String email = customUserDetails.getUsername();
        ResumeGitSummaryResponse response = resumeService.getWithGit(resumeId, email);

        return new BaseResponse<>("SUCCESS", response);
    }
}
