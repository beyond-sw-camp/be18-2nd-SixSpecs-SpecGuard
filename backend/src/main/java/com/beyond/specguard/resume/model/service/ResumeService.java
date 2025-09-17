package com.beyond.specguard.resume.model.service;

import com.beyond.specguard.common.exception.CustomException;
import com.beyond.specguard.common.exception.errorcode.CommonErrorCode;
import com.beyond.specguard.companytemplate.model.repository.CompanyTemplateRepository;
import com.beyond.specguard.event.ResumeSubmittedEvent;
import com.beyond.specguard.resume.auth.ResumeTempAuth;
import com.beyond.specguard.resume.model.dto.request.*;
import com.beyond.specguard.resume.model.dto.response.CompanyTemplateResponseResponse;
import com.beyond.specguard.resume.model.dto.response.ResumeBasicResponse;
import com.beyond.specguard.resume.model.dto.response.ResumeResponse;
import com.beyond.specguard.resume.model.dto.response.ResumeSubmitResponse;
import com.beyond.specguard.resume.model.entity.common.enums.Gender;
import com.beyond.specguard.resume.model.entity.common.enums.ResumeStatus;
import com.beyond.specguard.resume.model.entity.core.*;
import com.beyond.specguard.resume.exception.errorcode.ResumeErrorCode;
import com.beyond.specguard.resume.model.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
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
import java.util.*;
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
    private final ApplicationEventPublisher eventPublisher;


    //이력서 생성에서 create
    @Transactional
    public ResumeResponse create(ResumeCreateRequest req) {
        if (req.passwordHash() == null || req.passwordHash().isBlank()) {
            throw new CustomException(ResumeErrorCode.INVALID_REQUEST);
        }
        if (resumeRepository.existsByEmail(req.email())) {
            throw new CustomException(ResumeErrorCode.DUPLICATE_EMAIL);
        }

        String encoded = passwordEncoder.encode(req.passwordHash().trim());

        try {
            Resume saved = resumeRepository.save(
                    Resume.builder()
                            .templateId(req.templateId())
                            .status(ResumeStatus.DRAFT)
                            .name(req.name())
                            .phone(req.phone())
                            .email(req.email())
                            .passwordHash(encoded)
                            .build()
            );
            return toResumeResponse(saved);
        }catch (Exception e) {
            throw new CustomException(ResumeErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    //지원서 단건 조회에서 get
    @Transactional(readOnly = true)
    public ResumeResponse get(UUID resumeId, String secret) {
        try {
            if (!resumeRepository.existsById(resumeId)) {
                throw new CustomException(ResumeErrorCode.RESUME_NOT_FOUND);
            }
            Resume r = tempAuth.authenticate(resumeId, secret);
            return toResumeResponse(r);
        } catch (CustomException e) {
            throw e;
        }catch (Exception e) {
            throw new CustomException(ResumeErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    //지원서 목록 조회에서 list
    @Transactional(readOnly = true)
    public Page<ResumeResponse> list(Pageable pageable) {
        try {
            return resumeRepository.findAll(pageable).map(this::toResumeResponse);
        }catch (Exception e) {
            throw new CustomException(ResumeErrorCode.INTERNAL_SERVER_ERROR);
        }
    }


    //이력서 기본 정보 UPDATE/INSERT에서 upsertBasic
    @Transactional
    public ResumeBasicResponse upsertBasic(UUID resumeId, String secret, ResumeBasicCreateRequest req) {
        try {
            // 401/403/404는 tempAuth 내부에서 CustomException으로 던진다고 가정
            Resume resume = tempAuth.authenticate(resumeId, secret);

            Optional<ResumeBasic> opt = basicRepository.findByResume_Id(resumeId);

            ResumeBasic basic = opt.orElseGet(() -> basicRepository.save(
                    ResumeBasic.builder()
                            .resume(resume)
                            .englishName(req.englishName())
                            .gender(req.gender())
                            .birthDate(req.birthDate())
                            .nationality(req.nationality())
                            .applyField(req.applyField())
                            .profileImageUrl(req.profileImage())
                            .address(req.address())
                            .specialty(req.specialty())
                            .hobbies(req.hobbies())
                            .build()
            ));

            // 수정 경로 (null이면 변경 없음)
            if (opt.isPresent()) {
                if (req.englishName() != null) basic.changeEnglishName(req.englishName());
                if (req.gender() != null)      basic.changeGender(req.gender());
                if (req.birthDate() != null)   basic.changeBirthDate(req.birthDate());
                if (req.nationality() != null) basic.changeNationality(req.nationality());
                if (req.applyField() != null)  basic.changeApplyField(req.applyField());
                if (req.address() != null)     basic.changeAddress(req.address());
                if (req.specialty() != null)   basic.changeSpecialty(req.specialty());
                if (req.hobbies() != null)     basic.changeHobbies(req.hobbies());
                if (req.profileImage() != null) basic.changeProfileImageUrl(req.profileImage());
            }

            return toBasicResponse(basic, resumeId);

        } catch (CustomException e) {
            throw e;

        } catch (MaxUploadSizeExceededException e) {
            throw new CustomException(ResumeErrorCode.INTERNAL_SERVER_ERROR);

        } catch (Exception e) {
            if (isFileUploadError(e)) {
                throw new CustomException(ResumeErrorCode.FILE_UPLOAD_ERROR);
            }
            throw new CustomException(ResumeErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
    private boolean isFileUploadError(Exception e) {
        // 외부 스토리지 SDK 예외 타입/메시지 기반으로 판단
        return false;
    }


    //이력서 학력/경력/포트폴리오 링크 정보 UPDATE/INSERT
    @Transactional
    public void upsertAggregate(UUID resumeId, String secret, ResumeAggregateUpdateRequest req) {

        Resume resume = tempAuth.authenticate(resumeId, secret);


        try {
            Resume ref = resumeRepository.getReferenceById(resumeId);

            if (req.educations() != null) {
                validateEducationDuplicates(req.educations());

                educationRepository.deleteByResume_Id(resumeId);
                for (var d : req.educations()) {
                    if (d.schoolName() == null || d.major() == null) {
                        throw new CustomException(CommonErrorCode.INVALID_REQUEST);
                    }
                    educationRepository.save(mapEducation(ref, d));
                }
            }

            if (req.experiences() != null) {
                validateExperienceDuplicates(req.experiences());
                experienceRepository.deleteByResume_Id(resumeId);
                for (var d : req.experiences()) {
                    if (d.companyName() == null || d.position() == null) {
                        throw new CustomException(CommonErrorCode.INVALID_REQUEST);
                    }
                    experienceRepository.save(mapExperience(ref, d));
                }
            }


            if (req.links() != null) {
                validateLinkDuplicates(req.links());

                linkRepository.deleteByResume_Id(resumeId);
                for (var d : req.links()) {
                    if (d.url() == null || d.linkType() == null) {
                        throw new CustomException(CommonErrorCode.INVALID_REQUEST);
                    }
                    linkRepository.save(mapLink(ref, d));
                }
            }
        }catch (CustomException e) {
            throw e;
        }catch (Exception e) {
            throw new CustomException(ResumeErrorCode.INTERNAL_SERVER_ERROR);
        }

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
    public void upsertCertificates(UUID resumeId, String secret, List<ResumeCertificateUpsertRequest> certs) {
        tempAuth.authenticate(resumeId, secret);
        certificateRepository.deleteByResume_Id(resumeId);

        if(certs == null || certs.isEmpty()) return;

        Resume ref = resumeRepository.getReferenceById(resumeId);

        Set<String> seen = new HashSet<>();

        for (var d : certs) {
            if (d.certificateName() == null || d.certificateNumber() == null) {
                throw new CustomException(CommonErrorCode.INVALID_REQUEST);
            }
            String key = d.certificateName().trim().toLowerCase() + "|" + d.certificateNumber().trim().toLowerCase();
            if (!seen.add(key)) {
                throw new CustomException(ResumeErrorCode.DUPLICATE_ENTRY);
            }
            certificateRepository.save(mapCertificate(ref, d));
        }
    }

    //이력서 자기소개서 답변 UPDATE/INSERT saveTemplateResponses
    @Transactional
    public CompanyTemplateResponseResponse saveTemplateResponses(UUID resumeId, String secret, CompanyTemplateResponseCreateRequest ctr){
        Resume resume = tempAuth.authenticate(resumeId, secret);

        if (!resumeRepository.existsById(resumeId)) {
            throw new CustomException(ResumeErrorCode.RESUME_NOT_FOUND);
        }
        if (ctr == null || ctr.responses() == null) {
            throw new CustomException(ResumeErrorCode.INVALID_REQUEST);
        }
        if (ctr.responses().isEmpty()) {
            throw new CustomException(ResumeErrorCode.INVALID_REQUEST);
        }

        List<CompanyTemplateResponse> saved = new ArrayList<>();
        try{
            for (var i : ctr.responses()) {
                var opt = templateResponseRepository.findByResume_IdAndFieldId(resumeId, i.fieldId());
                CompanyTemplateResponse e = opt.orElseGet(() ->
                        CompanyTemplateResponse.builder()
                                .resume(resume)
                                .fieldId(i.fieldId())
                                .answer(i.answer())
                                .build()
                );
                if (opt.isPresent()) e.changeAnswer(i.answer());
                saved.add(templateResponseRepository.save(e));
            }
        }catch(Exception e){
            throw new CustomException(ResumeErrorCode.INTERNAL_SERVER_ERROR);
        }
        var items = saved.stream().map(e ->
                new CompanyTemplateResponseResponse.Item(
                        e.getId(), resumeId, e.getFieldId(), e.getAnswer(),
                        e.getCreatedAt(), e.getUpdatedAt()
                )).toList();

        return new CompanyTemplateResponseResponse(items.size(), items);
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
    public ResumeSubmitResponse submit(UUID resumeId, String secret, UUID companyId) {
        Resume resume = tempAuth.authenticate(resumeId, secret);
        if (!resumeRepository.existsById(resumeId)) {
            throw new CustomException(ResumeErrorCode.RESUME_NOT_FOUND);
        }
        if (companyId == null) {
            throw new CustomException(ResumeErrorCode.INVALID_REQUEST);
        }
        if (!basicRepository.existsByResume_Id(resumeId)) {
            throw new CustomException(ResumeErrorCode.INVALID_REQUEST);
        }
        if (submissionRepository.existsByResume_IdAndCompanyId(resumeId, companyId)) {
            throw new CustomException(ResumeErrorCode.ALREADY_SUBMITTED);
        }

        CompanyFormSubmission submission = submissionRepository.save(
                CompanyFormSubmission.builder()
                        .resume(resume)
                        .companyId(companyId)
                        .build()
        );

        resume.changeStatus(ResumeStatus.PENDING);

        eventPublisher.publishEvent(
                new ResumeSubmittedEvent(resume.getId(), resume.getTemplateId())
        );


        return new ResumeSubmitResponse(
                submission.getId(),
                resume.getId(),
                companyId,
                submission.getSubmittedAt(),
                resume.getStatus()
        );
    }


    // 커스텀 문항 임시저장 (answer: null/빈 허용)
    @Transactional
    public CompanyTemplateResponseResponse saveTemplateResponsesDraft(
            UUID resumeId, String secret, CompanyTemplateResponseDraftUpsertRequest dr
    ) {
        Resume resume = tempAuth.authenticate(resumeId, secret);
        if (dr == null || dr.responses() == null || dr.responses().isEmpty()) {
            throw new CustomException(ResumeErrorCode.INVALID_REQUEST);
        }

        Set<UUID> uniq = new HashSet<>();
        for (var item : dr.responses()) {
            if (item == null || item.fieldId() == null) throw new CustomException(ResumeErrorCode.INVALID_REQUEST);
            if (!uniq.add(item.fieldId())) throw new CustomException(ResumeErrorCode.DUPLICATE_ENTRY);
        }


        Map<UUID, String> answerMap = dr.responses().stream()
                .collect(Collectors.toMap(
                        CompanyTemplateResponseDraftUpsertRequest.Item::fieldId,
                        CompanyTemplateResponseDraftUpsertRequest.Item::answer
                ));

        List<CompanyTemplateResponse> saved = new ArrayList<>();
        try {
            for (UUID fieldId : uniq) {
                String ans = answerMap.get(fieldId);
                var opt = templateResponseRepository.findByResume_IdAndFieldId(resumeId, fieldId);
                CompanyTemplateResponse e = opt.orElseGet(() ->
                        CompanyTemplateResponse.builder()
                                .resume(resume)
                                .fieldId(fieldId)
                                .answer(ans)   // null/빈 허용
                                .build()
                );
                if (opt.isPresent()) e.changeAnswer(ans);
                saved.add(templateResponseRepository.save(e));
            }
        } catch (CustomException ce) {
            throw ce;
        } catch (Exception e) {
            throw new CustomException(ResumeErrorCode.INTERNAL_SERVER_ERROR);
        }

        var items = saved.stream().map(ent ->
                new CompanyTemplateResponseResponse.Item(
                        ent.getId(), resumeId, ent.getFieldId(),
                        ent.getAnswer(), ent.getCreatedAt(), ent.getUpdatedAt()
                )
        ).toList();

        return new CompanyTemplateResponseResponse(items.size(), items);
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
                                .gender(Gender.OTHER)
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




    //DTO -> Entity 매핑
    private ResumeEducation mapEducation(Resume r, ResumeEducationUpsertRequest d){
        return ResumeEducation.builder()
                .resume(r)
                .schoolType(d.schoolType())
                .schoolName(d.schoolName())
                .major(d.major())
                .degree(d.degree())
                .graduationStatus(d.graduationStatus())
                .admissionType(d.admissionType())
                .gpa(d.gpa())
                .maxGpa(d.maxGpa())
                .startDate(d.startDate())
                .endDate(d.endDate())
                .build();
    }
    private ResumeExperience mapExperience(Resume r, ResumeExperienceUpsertRequest d){
        return ResumeExperience.builder()
                .resume(r)
                .companyName(d.companyName())
                .department(d.department())
                .position(d.position())
                .responsibilities(d.responsibilities())
                .employmentStatus(d.employmentStatus())
                .startDate(d.startDate())
                .endDate(d.endDate())
                .build();
    }
    private ResumeCertificate mapCertificate(Resume r, ResumeCertificateUpsertRequest d){
        return ResumeCertificate.builder()
                .resume(r)
                .certificateName(d.certificateName())
                .certificateNumber(d.certificateNumber())
                .issuer(d.issuer())
                .issuedDate(d.issuedDate())
                .certUrl(d.certUrl())
                .build();
    }
    private ResumeLink mapLink(Resume r, ResumeLinkUpsertRequest d){
        return ResumeLink.builder()
                .resume(r)
                .url(d.url())
                .linkType(d.linkType())
                .build();
    }

    //Entity -> Dto
    private ResumeResponse toResumeResponse(Resume r) {
        return new ResumeResponse(
                r.getId(),
                r.getTemplateId(),
                r.getStatus(),
                r.getName(),
                r.getPhone(),
                r.getEmail(),
                r.getCreatedAt(),
                r.getUpdatedAt()
        );
    }
    private ResumeBasicResponse toBasicResponse(ResumeBasic b, UUID resumeId) {
        return new ResumeBasicResponse(
                b.getId(),
                resumeId,
                b.getEnglishName(),
                b.getGender().name(),
                b.getBirthDate(),
                b.getNationality(),
                b.getAddress(),
                b.getApplyField(),
                b.getSpecialty(),
                b.getHobbies(),
                b.getProfileImageUrl(),
                b.getCreatedAt()
        );
    }

}