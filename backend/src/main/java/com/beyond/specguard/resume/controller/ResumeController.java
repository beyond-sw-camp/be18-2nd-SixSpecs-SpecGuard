package com.beyond.specguard.resume.controller;

import com.beyond.specguard.common.exception.CustomException;
import com.beyond.specguard.resume.exception.errorcode.ResumeErrorCode;
import com.beyond.specguard.resume.model.dto.request.CompanyTemplateResponseDraftUpsertRequest;
import com.beyond.specguard.resume.model.dto.request.ResumeAggregateUpdateRequest;
import com.beyond.specguard.resume.model.dto.request.ResumeBasicCreateRequest;
import com.beyond.specguard.resume.model.dto.request.ResumeCertificateUpsertRequest;
import com.beyond.specguard.resume.model.dto.request.ResumeCreateRequest;
import com.beyond.specguard.resume.model.dto.request.ResumeSubmitRequest;
import com.beyond.specguard.resume.model.dto.response.CompanyTemplateResponseResponse;
import com.beyond.specguard.resume.model.dto.response.ResumeBasicResponse;
import com.beyond.specguard.resume.model.dto.response.ResumeResponse;
import com.beyond.specguard.resume.model.dto.response.ResumeSubmitResponse;
import com.beyond.specguard.resume.model.entity.core.Resume;
import com.beyond.specguard.resume.model.service.ResumeDetails;
import com.beyond.specguard.resume.model.service.ResumeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;
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
    @GetMapping("/{resumeId}")
    public ResumeResponse get(
            @PathVariable UUID resumeId,
            @AuthenticationPrincipal ResumeDetails resumeDetails
    ) {
        UUID templateId = resumeDetails.getResume().getTemplate().getId();
        String email = resumeDetails.getUsername();

        return resumeService.get(resumeId, email, templateId);
    }


    //지원서 목록 조회
    @Operation(
            summary = "지원서 목록 조회",
            description = "기업이 지원서 목록을 조회한다."
    )
    @GetMapping
    public Page<ResumeResponse> list(
            @PageableDefault Pageable pageable
    ) {
        if (pageable.getPageSize() > 100) {
            throw new CustomException(ResumeErrorCode.INVALID_PARAMETER);
        }

        return resumeService.list(pageable);
    }

    // 정렬 기준
    private static final Set<String> ALLOWED_SORT = Set.of(
            "createdAt", "updatedAt", "name", "status"
    );

    //이력서 기본 정보 UPDATE/INSERT
    @Operation(
            summary = "이력서 기본 정보 생성",
            description = "지원자가 이력서의 기본 정보를 최초 작성 및 임시저장한다."
    )
    @PostMapping("/basic")
    @ResponseStatus(HttpStatus.CREATED)
    public ResumeBasicResponse upsertBasic(
            @AuthenticationPrincipal  ResumeDetails resumeDetails,
            @Valid @RequestBody ResumeBasicCreateRequest req
    ) {
        UUID templateId = resumeDetails.getResume().getTemplate().getId();
        String email = resumeDetails.getUsername();
        Resume resume = resumeDetails.getResume();

        return resumeService.upsertBasic(resume, templateId, email, req);
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
            @Valid @RequestBody List<ResumeCertificateUpsertRequest> certificates
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


    //최종 제출
    @Operation(
            summary = "이력서 최종 제출",
            description = "제출 이력(company_form_submission)에 기록하고, 이력서 상태를 PENDING으로 전환합니다."
    )
    @PostMapping("/submit")
    @ResponseStatus(HttpStatus.CREATED)
    public ResumeSubmitResponse submit(
            @AuthenticationPrincipal ResumeDetails resumeDetails,
            @Valid @RequestBody ResumeSubmitRequest req
    ) {
        Resume resume = resumeDetails.getResume();

        return resumeService.submit(resume, req.companyId());
    }

    public record VerificationResult(boolean verified) {
    }


    // 프로필 이미지 업로드
    @Operation(
            summary = "프로필 이미지 업로드",
            description = "multipart/form-data로 이미지를 업로드하여 로컬 저장하고, profile_image_url을 갱신합니다."
    )
    @PostMapping(value = "/basic/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ResumeBasicResponse uploadProfileImage(
            @RequestHeader("X-Resume-Id") UUID resumeId,
            @RequestHeader("X-Resume-Secret") String secret,
            @RequestPart("file") MultipartFile file
    ) {
        return resumeService.uploadProfileImage(resumeId, secret, file);
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