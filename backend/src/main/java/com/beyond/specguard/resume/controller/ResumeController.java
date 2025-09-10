package com.beyond.specguard.resume.controller;

import com.beyond.specguard.common.exception.CustomException;
import com.beyond.specguard.resume.dto.request.*;
import com.beyond.specguard.resume.dto.response.CompanyTemplateResponseResponse;
import com.beyond.specguard.resume.dto.response.ResumeBasicResponse;
import com.beyond.specguard.resume.dto.response.ResumeResponse;
import com.beyond.specguard.resume.dto.response.ResumeSubmitResponse;
import com.beyond.specguard.resume.exception.errorcode.ResumeErrorCode;
import com.beyond.specguard.resume.service.ResumeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;
import java.util.UUID;

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
    public ResumeResponse create(@Valid @RequestBody ResumeCreateRequest req){
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
            @Parameter(name = "X-Resume-Secret", in = ParameterIn.HEADER, required = true,
                    description = "원문 비밀번호")
            @RequestHeader("X-Resume-Secret") String secret
    ){
        return resumeService.get(resumeId, secret);
    }


    //지원서 목록 조회
    @Operation(
            summary = "지원서 목록 조회",
            description = "기업 또는 지원자가 자신이 접근 가능한 지원서 목록을 조회한다."
    )
    @GetMapping
    public Page<ResumeResponse> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(value = "sort", required = false) List<String> sort
    ) {
        if (page < 0 || size < 1 || size > 100) {
            throw new CustomException(ResumeErrorCode.INVALID_PARAMETER);
        }

        Sort sortObj = buildSortOrThrow(sort); // 기본: createdAt,DESC
        Pageable pageable = PageRequest.of(page, size, sortObj);

        try {
            return resumeService.list(pageable);
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException(ResumeErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    // 정렬 기준
    private static final Set<String> ALLOWED_SORT = Set.of(
            "createdAt", "updatedAt", "name", "status"
    );

    private Sort buildSortOrThrow(List<String> sort) {
        // 기본 정렬: createdAt desc
        if (sort == null || sort.isEmpty()) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }

        Sort result = Sort.unsorted();
        for (String token : sort) {
            if (token == null || token.isBlank()) {
                throw new CustomException(ResumeErrorCode.INVALID_PARAMETER);
            }
            String[] parts = token.split(",");
            String prop = parts[0].trim();

            if (!ALLOWED_SORT.contains(prop)) {
                throw new CustomException(ResumeErrorCode.INVALID_PARAMETER);
            }

            Sort.Direction dir = Sort.Direction.ASC;
            if (parts.length > 1) {
                String d = parts[1].trim().toLowerCase();
                if (!d.equals("asc") && !d.equals("desc")) {
                    throw new CustomException(ResumeErrorCode.INVALID_PARAMETER);
                }
                dir = "desc".equals(d) ? Sort.Direction.DESC : Sort.Direction.ASC;
            }
            result = result.and(Sort.by(dir, prop));
        }
        return result;
    }

    //이력서 기본 정보 UPDATE/INSERT
    @Operation(
            summary = "이력서 기본 정보 생성",
            description = "지원자가 이력서의 기본 정보를 최초 작성 및 임시저장한다."
    )
    @PostMapping("/basic")
    @ResponseStatus(HttpStatus.CREATED)
    public ResumeBasicResponse upsertBasic(
            @RequestHeader("X-Resume-Id") UUID resumeId,
            @RequestHeader("X-Resume-Secret") String secret,
            @Valid @RequestBody ResumeBasicCreateRequest req
    ) {
        if(req.englishName() == null ||
        req.birthDate() ==null ||
        req.gender() == null ||
        req.nationality() == null ||
        req.applyField() == null ||
        req.profileImage() == null ||
        req.address() == null){
            throw new CustomException(ResumeErrorCode.INVALID_REQUEST);
        }
        return resumeService.upsertBasic(resumeId, secret, req);
    }


    //이력서 학력/경력/포트폴리오 링크 정보 UPDATE/INSERT
    @Operation(
            summary = "이력서 학력/경력/포트폴리오 링크 정보 생성",
            description = " 지원자가 한 탭에서 학력, 경력, 포트폴리오 링크를 모두 입력하고 임시저장한다."
    )
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
    @Operation(
            summary = "이력서 자격증 정보 생성",
            description = " 지원자가 이력서에 여러 개의 자격증 정보를 한 번에 입력 및 임시저장한다."
    )
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
    @Operation(
            summary = "이력서 자기소개서 답변 생성",
            description = "지원자가 한 페이지에서 작성한 여러 자기소개서 문항 답변을 한 번에 저장한다."
    )
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


    //최종 제출
    @Operation(
            summary = "이력서 최종 제출",
            description = "제출 이력(company_form_submission)에 기록하고, 이력서 상태를 PENDING으로 전환합니다.",
            parameters = {
                    @Parameter(name = "X-Resume-Id", in = ParameterIn.HEADER, required = true, description = "내 이력서 ID(UUID)"),
                    @Parameter(name = "X-Resume-Secret", in = ParameterIn.HEADER, required = true, description = "원문 비밀번호(임시 인증)")
            }
    )
    @PostMapping("/submit")
    @ResponseStatus(HttpStatus.CREATED)
    public ResumeSubmitResponse submit(
            @RequestHeader("X-Resume-Id") UUID resumeId,
            @RequestHeader("X-Resume-Secret") String secret,
            @Valid @RequestBody ResumeSubmitRequest req
    ) {
        return resumeService.submit(resumeId, secret, req.companyId());
    }

    public record VerificationResult(boolean verified) {}

    // 커스텀 질문 임시저장
    //(resume_id, field_id) 기준 upsert
    @Operation(
            summary = "이력서 커스텀 문항 임시저장",
            description = "자기소개서/역량기술서 등 기업 커스텀 문항 답변을 임시저장합니다. - 임시 저장 시 answer는 빈 문자열/NULL 허용"
    )
    @PostMapping("/template-responses/draft")
    @ResponseStatus(HttpStatus.CREATED)
    public CompanyTemplateResponseResponse saveTemplateResponsesDraft(
            @RequestHeader("X-Resume-Id") UUID resumeId,
            @RequestHeader("X-Resume-Secret") String secret,
            @Valid @org.springframework.web.bind.annotation.RequestBody CompanyTemplateResponseDraftUpsertRequest req
    ) {
        return resumeService.saveTemplateResponsesDraft(resumeId, secret, req);
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


}