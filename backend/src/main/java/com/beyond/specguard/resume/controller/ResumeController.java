package com.beyond.specguard.resume.controller;

import com.beyond.specguard.auth.model.service.CustomUserDetails;
import com.beyond.specguard.resume.dto.request.*;
import com.beyond.specguard.resume.dto.response.CompanyTemplateResponseResponse;
import com.beyond.specguard.resume.dto.response.ResumeBasicResponse;
import com.beyond.specguard.resume.dto.response.ResumeCertificateResponse;
import com.beyond.specguard.resume.dto.response.ResumeResponse;
import com.beyond.specguard.resume.service.ResumeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/resumes")
public class ResumeController {
    private final ResumeService resumeService;


    //이력서 생성
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResumeResponse create(@Valid @RequestBody ResumeCreateRequest req){
        return resumeService.create(req);
    }
    //지원서 단건 조회
    @GetMapping("/{resumeId}")
    public ResumeResponse get(
            @PathVariable UUID resumeId,
            @Parameter(name = "X-Resume-Secret", in = ParameterIn.HEADER, required = true,
                    description = "원문 비밀번호")
            @RequestHeader("X-Resume-Secret") String secret
    ){
        return resumeService.get(resumeId, secret);
    }


    //지원서 목록 조회
    @GetMapping
    public Page<ResumeResponse> list(Pageable pageable) {
        return resumeService.list(pageable);
    }


    //이력서 기본 정보 UPDATE/INSERT
    @PostMapping("/basic")
    @ResponseStatus(HttpStatus.CREATED)
    public ResumeBasicResponse upsertBasic(
            @RequestHeader("X-Resume-Id") UUID resumeId,
            @RequestHeader("X-Resume-Secret") String secret,
            @Valid @RequestBody ResumeBasicCreateRequest req
    ) {
        return resumeService.upsertBasic(resumeId, secret, req);
    }


    //이력서 학력/경력/포트폴리오 링크 정보 UPDATE/INSERT
    @PostMapping("/edu-exp-link")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void upsertEduExpLink(
            @RequestHeader("X-Resume-Id") UUID resumeId,
            @RequestHeader("X-Resume-Secret") String secret,
            @Valid @RequestBody ResumeAggregateUpdateRequest req
    ) {
        resumeService.upsertAggregate(resumeId, secret, req);
    }

    //이력서 자격증 정보 UPDATE/INSERT
    @PostMapping("/certificates")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void upsertCertificates(
            @RequestHeader("X-Resume-Id") UUID resumeId,
            @RequestHeader("X-Resume-Secret") String secret,
            @Valid @RequestBody List<ResumeCertificateUpsertRequest> certificates
    ) {
        resumeService.upsertCertificates(resumeId, secret, certificates);
    }


    //이력서 자기소개서 답변 UPDATE/INSERT
    @PostMapping("/template-responses")
    @ResponseStatus(HttpStatus.CREATED)
    public CompanyTemplateResponseResponse saveTemplateResponses(
            @RequestHeader("X-Resume-Id") UUID resumeId,
            @RequestHeader("X-Resume-Secret") String secret,
            @Valid @RequestBody CompanyTemplateResponseCreateRequest req
    ) {
        return resumeService.saveTemplateResponses(resumeId, secret, req);
    }

    @Operation(
            summary = "자격증 진위 여부 검증",
            parameters = {
                    @Parameter(name = "X-Resume-Id", in = ParameterIn.HEADER, required = true, description = "내 이력서 ID(UUID)"),
                    @Parameter(name = "X-Resume-Secret", in = ParameterIn.HEADER, required = true, description = "원문 비밀번호")
            }
    )
    //자격증 진위 여부 검증 요청
    @PostMapping("/certificates/{certificateId}/verify")
    public VerificationResult verifyCertificate(
            @RequestHeader("X-Resume-Id") UUID resumeId,
            @RequestHeader("X-Resume-Secret") String secret,
            @PathVariable UUID certificateId
    ) {
        boolean ok = resumeService.verifyCertificate(resumeId, secret, certificateId);
        return new VerificationResult(ok);
    }

    public record VerificationResult(boolean verified) {}




}