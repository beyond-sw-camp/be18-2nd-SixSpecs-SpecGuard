package com.beyond.specguard.resume.model.service;

import com.beyond.specguard.common.exception.CustomException;
import com.beyond.specguard.companytemplate.exception.ErrorCode.CompanyTemplateErrorCode;
import com.beyond.specguard.companytemplate.model.entity.CompanyTemplate;
import com.beyond.specguard.companytemplate.model.repository.CompanyTemplateFieldRepository;
import com.beyond.specguard.companytemplate.model.repository.CompanyTemplateRepository;
import com.beyond.specguard.resume.auth.ResumeTempAuth;
import com.beyond.specguard.resume.exception.errorcode.ResumeErrorCode;
import com.beyond.specguard.resume.model.dto.request.CompanyTemplateResponseDraftUpsertRequest;
import com.beyond.specguard.resume.model.dto.request.ResumeAggregateUpdateRequest;
import com.beyond.specguard.resume.model.dto.request.ResumeBasicCreateRequest;
import com.beyond.specguard.resume.model.dto.request.ResumeCertificateUpsertRequest;
import com.beyond.specguard.resume.model.dto.request.ResumeCreateRequest;
import com.beyond.specguard.resume.model.dto.request.ResumeEducationUpsertRequest;
import com.beyond.specguard.resume.model.dto.request.ResumeExperienceUpsertRequest;
import com.beyond.specguard.resume.model.dto.request.ResumeLinkUpsertRequest;
import com.beyond.specguard.resume.model.dto.response.CompanyTemplateResponseResponse;
import com.beyond.specguard.resume.model.dto.response.ResumeBasicResponse;
import com.beyond.specguard.resume.model.dto.response.ResumeResponse;
import com.beyond.specguard.resume.model.dto.response.ResumeSubmitResponse;
import com.beyond.specguard.resume.model.entity.CompanyFormSubmission;
import com.beyond.specguard.resume.model.entity.CompanyTemplateResponse;
import com.beyond.specguard.resume.model.entity.Resume;
import com.beyond.specguard.resume.model.entity.ResumeBasic;
import com.beyond.specguard.resume.model.entity.ResumeCertificate;
import com.beyond.specguard.resume.model.entity.ResumeEducation;
import com.beyond.specguard.resume.model.entity.ResumeExperience;
import com.beyond.specguard.resume.model.entity.ResumeLink;
import com.beyond.specguard.resume.model.repository.CompanyFormSubmissionRepository;
import com.beyond.specguard.resume.model.repository.CompanyTemplateResponseRepository;
import com.beyond.specguard.resume.model.repository.ResumeBasicRepository;
import com.beyond.specguard.resume.model.repository.ResumeCertificateRepository;
import com.beyond.specguard.resume.model.repository.ResumeEducationRepository;
import com.beyond.specguard.resume.model.repository.ResumeExperienceRepository;
import com.beyond.specguard.resume.model.repository.ResumeLinkRepository;
import com.beyond.specguard.resume.model.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class ResumeService {
    private final ResumeRepository resumeRepository;
    private final ResumeBasicRepository basicRepository;
    private final ResumeEducationRepository educationRepository;
    private final ResumeExperienceRepository experienceRepository;
    private final ResumeCertificateRepository certificateRepository;
    private final ResumeLinkRepository linkRepository;
    private final CompanyTemplateResponseRepository templateResponseRepository;
    private final CompanyFormSubmissionRepository submissionRepository;
    private final CompanyTemplateRepository companyTemplateRepository;
    private final PasswordEncoder passwordEncoder;
    private final ResumeTempAuth tempAuth;
    private final LocalFileStorageService storageService;
    private final CompanyTemplateFieldRepository companyTemplateFieldRepository;
    private final CompanyTemplateResponseRepository companyTemplateResponseRepository;

    //이력서 생성에서 create
    @Transactional
    public ResumeResponse create(ResumeCreateRequest req) {
        CompanyTemplate companyTemplate = companyTemplateRepository.findById(req.templateId())
                .orElseThrow(() -> new CustomException(CompanyTemplateErrorCode.TEMPLATE_NOT_FOUND));

        if (resumeRepository.existsByEmailAndTemplateId(req.email(), req.templateId())) {
            throw new CustomException(ResumeErrorCode.DUPLICATE_EMAIL);
        }

        Resume r = req.toEntity(companyTemplate);

        r.encodePassword(passwordEncoder.encode(req.password().trim()));

        Resume saved = resumeRepository.saveAndFlush(r);

        return ResumeResponse.fromEntity(saved);
    }

    private void validateOwnerShip(Resume resume, String username, UUID templateId) {
        if (!resume.getEmail().equals(username) || !resume.getTemplate().getId().equals(templateId)) {
            throw new CustomException(ResumeErrorCode.ACCESS_DENIED);
        }
    }

    //지원서 단건 조회에서 get
    @Transactional(readOnly = true)
    public ResumeResponse get(UUID resumeId, String username, UUID templateId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new CustomException(ResumeErrorCode.RESUME_NOT_FOUND));

        validateOwnerShip(resume, username, templateId);

        return ResumeResponse.fromEntity(resume);
    }

    //지원서 목록 조회에서 list
    @Transactional(readOnly = true)
    public Page<ResumeResponse> list(Pageable pageable) {
        return resumeRepository.findAll(pageable).map(ResumeResponse::fromEntity);
    }

    //이력서 기본 정보 UPDATE/INSERT에서 upsertBasic
    @Transactional
    public ResumeBasicResponse upsertBasic(Resume resume, UUID templateId, String email, ResumeBasicCreateRequest req) {
        try {
            validateOwnerShip(resume, email, templateId);

            Optional<ResumeBasic> opt = basicRepository.findByResume_Id(resume.getId());

            ResumeBasic basic = opt.orElseGet(() -> basicRepository.saveAndFlush(req.toEntity(resume)));

            // 수정 경로 (null이면 변경 없음)
            if (opt.isPresent()) {
                basic.update(req);
            }

            return ResumeBasicResponse.fromEntity(basic);

        } catch (MaxUploadSizeExceededException e) {
            throw new CustomException(ResumeErrorCode.INTERNAL_SERVER_ERROR);
        }
        // TODO: 외부 스토리지 SDK 예외 타입/메시지 기반으로 판단
    }

    //이력서 학력/경력/포트폴리오 링크 정보 UPDATE/INSERT
    @Transactional
    public void upsertAggregate(UUID resumeId, UUID templateId, String email, ResumeAggregateUpdateRequest req) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new CustomException(ResumeErrorCode.RESUME_NOT_FOUND));

        validateOwnerShip(resume, email, templateId);

        if (req.educations() != null) {
            validateEducationDuplicates(req.educations());

            List<ResumeEducation> updatedFields = new ArrayList<>();

            Map<UUID, ResumeEducationUpsertRequest> dtoMap = req.educations().stream()
                    .filter(f -> f.id() != null)
                    .collect(Collectors.toMap(ResumeEducationUpsertRequest::id, f -> f));

            // 6. 업데이트
            for (ResumeEducation existing : resume.getResumeEducations()) {
                if (dtoMap.containsKey(existing.getId())) {
                    existing.update(dtoMap.get(existing.getId()));
                    updatedFields.add(existing);
                }
            }

            List<ResumeEducation> newResumeEducations = req.educations().stream()
                            .filter(f -> f.id() == null)
                            .map(e -> e.toEntity(resume))
                            .toList();

            resume.getResumeEducations().clear();

            updatedFields.addAll(newResumeEducations);

            resume.getResumeEducations().addAll(updatedFields);
        }

        if (req.experiences() != null) {
            validateExperienceDuplicates(req.experiences());

            List<ResumeExperience> updatedFields = new ArrayList<>();

            Map<UUID, ResumeExperienceUpsertRequest> dtoMap = req.experiences().stream()
                    .filter(f -> f.id() != null)
                    .collect(Collectors.toMap(ResumeExperienceUpsertRequest::id, f -> f));

            // 6. 업데이트
            for (ResumeExperience existing : resume.getResumeExperiences()) {
                if (dtoMap.containsKey(existing.getId())) {
                    // 업데이트
                    existing.update(dtoMap.get(existing.getId()));
                    updatedFields.add(existing);
                }
            }

            List<ResumeExperience> newResumeExperience = req.experiences().stream()
                    .filter(f -> f.id() == null)
                    .map(e -> e.toEntity(resume))
                    .toList();

            resume.getResumeExperiences().clear();

            updatedFields.addAll(newResumeExperience);

            resume.getResumeExperiences().addAll(updatedFields);
        }


        if (req.links() != null) {
            validateLinkDuplicates(req.links());

            List<ResumeLink> updatedFields = new ArrayList<>();

            Map<UUID, ResumeLinkUpsertRequest> dtoMap = req.links().stream()
                    .filter(f -> f.id() != null)
                    .collect(Collectors.toMap(ResumeLinkUpsertRequest::id, f -> f));

            // 6. 업데이트
            for (ResumeLink existing : resume.getResumeLinks()) {
                if (dtoMap.containsKey(existing.getId())) {
                    // 업데이트
                    existing.update(dtoMap.get(existing.getId()));
                    updatedFields.add(existing);
                }
            }

            List<ResumeLink> newResumeLinks = req.links().stream()
                    .filter(f -> f.id() == null)
                    .map(l -> l.toEntity(resume))
                    .toList();

            resume.getResumeLinks().clear();

            updatedFields.addAll(newResumeLinks);

            resume.getResumeLinks().addAll(updatedFields);
        }

        resumeRepository.saveAndFlush(resume);
    }


    // 학력 중복/기간 검증
    private void validateEducationDuplicates(List<ResumeEducationUpsertRequest> educations) {
        Set<String> keys = new HashSet<>();
        for (var edu : educations) {
            if (edu.startDate() != null && edu.endDate() != null && edu.startDate().isAfter(edu.endDate())) {
                throw new CustomException(ResumeErrorCode.INVALID_REQUEST);
            }
            String key = edu.schoolName() + "|" + edu.startDate() + "|" + edu.endDate();
            if (!keys.add(key)) {
                throw new CustomException(ResumeErrorCode.DUPLICATE_ENTRY);
            }
        }
    }

    // 경력 중복/기간 검증
    private void validateExperienceDuplicates(List<ResumeExperienceUpsertRequest> experiences) {
        Set<String> keys = new HashSet<>();
        for (var exp : experiences) {
            if (exp.startDate() != null && exp.endDate() != null && exp.startDate().isAfter(exp.endDate())) {
                throw new CustomException(ResumeErrorCode.INVALID_REQUEST);
            }
            String key = exp.companyName() + "|" + exp.startDate() + "|" + exp.endDate();
            if (!keys.add(key)) {
                throw new CustomException(ResumeErrorCode.DUPLICATE_ENTRY);
            }
        }
    }

    // 링크 중복 검증
    private void validateLinkDuplicates(List<ResumeLinkUpsertRequest> links) {
        Set<String> keys = new HashSet<>();
        for (var link : links) {
            if (link.url() == null || link.url().isBlank()) {
                throw new CustomException(ResumeErrorCode.INVALID_REQUEST);
            }
            String norm = link.url().trim().toLowerCase();
            if (!keys.add(norm)) {
                throw new CustomException(ResumeErrorCode.DUPLICATE_ENTRY);
            }
        }
    }

    //이력서 자격증 정보 UPDATE/INSERT upsertCertificates
    @Transactional
    public void upsertCertificates(UUID resumeId, UUID templateId, String email, List<ResumeCertificateUpsertRequest> certs) {
        if(certs == null || certs.isEmpty()) return;

        validateResumeCertificate(certs);

        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new CustomException(ResumeErrorCode.RESUME_NOT_FOUND));

        validateOwnerShip(resume, email, templateId);

        List<ResumeCertificate> updatedFields = new ArrayList<>();

        Map<UUID, ResumeCertificateUpsertRequest> dtoMap = certs.stream()
                .filter(f -> f.id() != null)
                .collect(Collectors.toMap(ResumeCertificateUpsertRequest::id, f -> f));

        // 6. 업데이트
        for (ResumeCertificate existing : resume.getResumeCertificates()) {
            if (dtoMap.containsKey(existing.getId())) {
                // 업데이트
                existing.update(dtoMap.get(existing.getId()));
                updatedFields.add(existing);
            }
        }

        List<ResumeCertificate> newCertificates = certs.stream()
                .filter(f -> f.id() == null)
                .map(l -> l.toEntity(resume))
                .toList();

        resume.getResumeCertificates().clear();

        updatedFields.addAll(newCertificates);

        resume.getResumeCertificates().addAll(updatedFields);

        resumeRepository.saveAndFlush(resume);
    }

    private void validateResumeCertificate(List<ResumeCertificateUpsertRequest> certs) {
        Set<String> seen = new HashSet<>();

        for (var d : certs) {
            String key = d.certificateName().trim().toLowerCase() + "|" + d.certificateNumber().trim().toLowerCase();
            if (!seen.add(key)) {
                throw new CustomException(ResumeErrorCode.DUPLICATE_ENTRY);
            }
        }
    }

    //이력서 자기소개서 답변 UPDATE/INSERT saveTemplateResponses
    @Transactional
    public CompanyTemplateResponseResponse saveTemplateResponses(Resume resume, UUID templateId, String email, CompanyTemplateResponseDraftUpsertRequest req){
        validateOwnerShip(resume, email, templateId);

        List<CompanyTemplateResponse> updatedFields = new ArrayList<>();

        Map<UUID, CompanyTemplateResponseDraftUpsertRequest.Item> dtoMap = req.responses().stream()
                .filter(f -> f.id() != null)
                .collect(Collectors.toMap(CompanyTemplateResponseDraftUpsertRequest.Item::id, f -> f));

        // 6. 업데이트
        for (CompanyTemplateResponse existing : resume.getTemplateResponses()) {
            if (dtoMap.containsKey(existing.getId())) {
                // 업데이트
                existing.update(dtoMap.get(existing.getId()));
                updatedFields.add(existing);
            }
        }

        List<CompanyTemplateResponse> newResponses = req.responses().stream()
                .filter(f -> f.id() == null)
                .map(l -> l.toEntity(resume, companyTemplateFieldRepository.getReferenceById(l.fieldId())))
                .toList();

        resume.getTemplateResponses().clear();

        updatedFields.addAll(newResponses);

        resume.getTemplateResponses().addAll(updatedFields);

        return CompanyTemplateResponseResponse.builder()
                        .savedCount(updatedFields.size())
                        .responses(updatedFields.stream().map(CompanyTemplateResponseResponse.Item::fromEntity).toList())
                        .build();
    }

    //자격증 진위 여부 검증 요청 -> 지금은 자격증 존재하면 true
    @Transactional(readOnly = true)
    public boolean verifyCertificate(UUID resumeId, String secret, UUID certificateId) {
        tempAuth.authenticate(resumeId, secret);
        //기업 사용자 여부 확인에 대한 예외 필요 -> HOW???


        return certificateRepository.findByIdAndResume_Id(certificateId, resumeId).isPresent();
    }


    //최종 제출
    @Transactional
    public ResumeSubmitResponse submit(Resume resume, UUID companyId) {
        if (resume.getResumeBasic() == null) {
            throw new CustomException(ResumeErrorCode.INVALID_REQUEST);
        }

        if (submissionRepository.existsByResume_IdAndCompanyId(resume.getId(), companyId)) {
            throw new CustomException(ResumeErrorCode.ALREADY_SUBMITTED);
        }

        CompanyFormSubmission submission = submissionRepository.saveAndFlush(
                CompanyFormSubmission.builder()
                        .resume(resume)
                        .companyId(companyId)
                        .build()
        );

        resume.setStatusPending();

        return ResumeSubmitResponse.fromEntity(submission);
    }

    // 프로필 이미지 업로드 -> 로컬 저장 & URL DB 저장/갱신
    @Transactional
    public ResumeBasicResponse uploadProfileImage(UUID resumeId, String secret, MultipartFile file) {
        Resume resume = tempAuth.authenticate(resumeId, secret);
        if (file == null) throw new CustomException(ResumeErrorCode.INVALID_REQUEST);

        String url = storageService.saveProfileImage(resumeId, file);

        ResumeBasic basic = basicRepository.findByResume_Id(resumeId)
                .orElseGet(() -> basicRepository.save(
                        ResumeBasic.builder()
                                .resume(resume)
                                .englishName(" ")
                                .gender(ResumeBasic.Gender.OTHER)
                                .birthDate(LocalDate.of(1900, 1, 1))
                                .nationality(" ")
                                .applyField(" ")
                                .address(" ")
                                .profileImageUrl(url)
                                .build()
                ));
        basic.changeProfileImageUrl(url);

        return new ResumeBasicResponse(
                basic.getId(), resumeId, basic.getEnglishName(), basic.getGender().name(),
                basic.getBirthDate(), basic.getNationality(), basic.getAddress(),
                basic.getApplyField(), basic.getSpecialty(), basic.getHobbies(),
                basic.getProfileImageUrl(), basic.getCreatedAt()
        );
    }


    //삭제
    @Transactional
    public int cleanupExpiredUnsubmittedResumes(int batchSize) {
        int totalDeleted = 0;
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));

        var expiredTemplateIds = companyTemplateRepository.findExpiredTemplateIds(now);
        if (expiredTemplateIds.isEmpty()) return 0;

        Pageable limit = PageRequest.of(0, batchSize);

        while (true) {
            var targetIds = resumeRepository.findUnsubmittedIdsByTemplateIds(expiredTemplateIds, limit);
            if (targetIds.isEmpty()) break;

            for (UUID resumeId : targetIds) {
                // 자식 -> 부모 순으로 삭제 + 파일 정리
                cascadeDeleteByResume(resumeId);
                totalDeleted++;
            }

        }
        return totalDeleted;
    }


    private void cascadeDeleteByResume(UUID resumeId) {
        templateResponseRepository.deleteByResume_Id(resumeId);
        certificateRepository.deleteByResume_Id(resumeId);
        linkRepository.deleteByResume_Id(resumeId);
        experienceRepository.deleteByResume_Id(resumeId);
        educationRepository.deleteByResume_Id(resumeId);
        basicRepository.deleteByResume_Id(resumeId);
        submissionRepository.deleteByResume_Id(resumeId);

        resumeRepository.deleteById(resumeId);
        storageService.deleteAllProfileImages(resumeId);
    }
}