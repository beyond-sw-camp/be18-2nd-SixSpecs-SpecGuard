package com.beyond.specguard.resume.controller;

import com.beyond.specguard.company.common.model.service.CustomUserDetails;
import com.beyond.specguard.resume.model.dto.request.CompanyTemplateResponseDraftUpsertRequest;
import com.beyond.specguard.resume.model.dto.request.ResumeAggregateUpdateRequest;
import com.beyond.specguard.resume.model.dto.request.ResumeBasicCreateRequest;
import com.beyond.specguard.resume.model.dto.request.ResumeCertificateUpsertRequest;
import com.beyond.specguard.resume.model.dto.request.ResumeCreateRequest;
import com.beyond.specguard.resume.model.dto.response.CompanyTemplateResponseResponse;
import com.beyond.specguard.resume.model.dto.response.ResumeBasicResponse;
import com.beyond.specguard.resume.model.dto.response.ResumeResponse;
import com.beyond.specguard.resume.model.dto.response.ResumeSubmitResponse;
import com.beyond.specguard.resume.model.entity.Resume;
import com.beyond.specguard.resume.model.service.ResumeDetails;
import com.beyond.specguard.resume.model.service.ResumeService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/resumes")
public class ResumeController {
    private final ResumeService resumeService;

    //이력서 생성
    @Operation(
            summary = "이력서 생성",
            description = "지원자가 최초로 회원가입 하며 이력서 생성 시 이력서 데이터를 생성한다."
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResumeResponse create(
            @Valid @RequestBody ResumeCreateRequest req
    ) {
        return resumeService.create(req);
    }



    //지원서 단건 조회
    @Operation(
            summary = "지원서 단건 조회",
            description = "특정 지원서(resume.id) 단건 조회."
    )
    @GetMapping
    public ResumeResponse getResume(
            @AuthenticationPrincipal ResumeDetails resumeDetails
    ) {
        UUID templateId = resumeDetails.getResume().getTemplate().getId();
        String email = resumeDetails.getUsername();
        UUID resumeId = resumeDetails.getResume().getId();

        return resumeService.get(resumeId, email, templateId);
    }

    //이력서 기본 정보 UPDATE/INSERT
    @Operation(
            summary = "이력서 기본 정보 생성",
            description = "지원자가 이력서의 기본 정보를 최초 작성 및 임시저장한다."
    )
    @PostMapping(value = "/basic", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ResumeBasicResponse upsertBasic(
            @AuthenticationPrincipal  ResumeDetails resumeDetails,
            @RequestPart("basic") @Valid ResumeBasicCreateRequest req,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage
    ) throws IOException {
        UUID templateId = resumeDetails.getResume().getTemplate().getId();
        String email = resumeDetails.getUsername();
        UUID resumeId = resumeDetails.getResume().getId();

        return resumeService.upsertBasic(resumeId, templateId, email, req, profileImage);
    }


    //이력서 학력/경력/포트폴리오 링크 정보 UPDATE/INSERT
    @Operation(
            summary = "이력서 학력/경력/포트폴리오 링크 정보 생성",
            description = " 지원자가 한 탭에서 학력, 경력, 포트폴리오 링크를 모두 입력하고 임시저장한다."
    )
    @PostMapping("/edu-exp-link")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void upsertEduExpLink(
            @AuthenticationPrincipal ResumeDetails resumeDetails,
            @Valid @RequestBody ResumeAggregateUpdateRequest req
    ) {

        UUID templateId = resumeDetails.getResume().getTemplate().getId();
        String email = resumeDetails.getUsername();
        UUID resumeId = resumeDetails.getResume().getId();

        log.info("upsertEduExpLink, templateId : {}, email : {}, resumeId : {}", templateId, email, resumeId);

        resumeService.upsertAggregate(resumeId, templateId, email, req);
    }

    //이력서 자격증 정보 UPDATE/INSERT
    @Operation(
            summary = "이력서 자격증 정보 생성",
            description = " 지원자가 이력서에 여러 개의 자격증 정보를 한 번에 입력 및 임시저장한다."
    )
    @PostMapping("/certificates")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void upsertCertificates(
            @AuthenticationPrincipal ResumeDetails resumeDetails,
            @Valid @RequestBody ResumeCertificateUpsertRequest certificates
    ) {
        UUID templateId = resumeDetails.getResume().getTemplate().getId();
        String email = resumeDetails.getUsername();
        UUID resumeId = resumeDetails.getResume().getId();

        resumeService.upsertCertificates(resumeId, templateId, email, certificates);
    }


    //이력서 자기소개서 답변 UPDATE/INSERT
    @Operation(
            summary = "이력서 자기소개서 답변 생성",
            description = "지원자가 한 페이지에서 작성한 여러 자기소개서 문항 답변을 한 번에 저장한다."
    )
    @PostMapping("/template-responses")
    @ResponseStatus(HttpStatus.CREATED)
    public CompanyTemplateResponseResponse saveTemplateResponses(
            @AuthenticationPrincipal ResumeDetails resumeDetails,
            @Valid @RequestBody CompanyTemplateResponseDraftUpsertRequest req
    ) {
        UUID templateId = resumeDetails.getResume().getTemplate().getId();
        String email = resumeDetails.getUsername();
        Resume resume = resumeDetails.getResume();

        return resumeService.saveTemplateResponses(resume, templateId, email, req);
    }

    //최종 제출
    @Operation(
            summary = "이력서 최종 제출",
            description = "제출 이력(company_form_submission)에 기록하고, 이력서 상태를 PENDING으로 전환합니다."
    )
    @PostMapping("/submit")
    @ResponseStatus(HttpStatus.CREATED)
    public ResumeSubmitResponse submit(
            @AuthenticationPrincipal ResumeDetails resumeDetails
    ) {
        UUID resumeId = resumeDetails.getResume().getId();

        return resumeService.submit(resumeId);
    }

    // 세션 기반의 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return ResponseEntity.noContent().build();
    }

}