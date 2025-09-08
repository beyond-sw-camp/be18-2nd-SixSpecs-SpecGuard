package com.beyond.specguard.resume.service;

import com.beyond.specguard.common.exception.CustomException;
import com.beyond.specguard.resume.auth.ResumeTempAuth;
import com.beyond.specguard.resume.dto.request.*;
import com.beyond.specguard.resume.dto.response.CompanyTemplateResponseResponse;
import com.beyond.specguard.resume.dto.response.ResumeBasicResponse;
import com.beyond.specguard.resume.dto.response.ResumeCertificateResponse;
import com.beyond.specguard.resume.dto.response.ResumeResponse;
import com.beyond.specguard.resume.entity.common.enums.ResumeStatus;
import com.beyond.specguard.resume.entity.core.*;
import com.beyond.specguard.resume.exception.errorcode.ResumeErrorCode;
import com.beyond.specguard.resume.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
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


    private final PasswordEncoder passwordEncoder;
    private final ResumeTempAuth tempAuth;



    //이력서 생성에서 create
    @Transactional
    public ResumeResponse create(ResumeCreateRequest req) {
        if (req.passwordHash() == null || req.passwordHash().isBlank()) {
            throw new CustomException(ResumeErrorCode.INVALID_REQUEST);
        }
        String encoded = passwordEncoder.encode(req.passwordHash().trim());

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
    }

    //지원서 단건 조회에서 get
    @Transactional(readOnly = true)
    public ResumeResponse get(UUID resumeId, String secret) {
        Resume r = tempAuth.authenticate(resumeId, secret);
        return toResumeResponse(r);
    }

    //지원서 목록 조회에서 list
    @Transactional(readOnly = true)
    public Page<ResumeResponse> list(Pageable pageable) {
        return resumeRepository.findAll(pageable).map(this::toResumeResponse);
    }


    //이력서 기본 정보 UPDATE/INSERT에서 upsertBasic
    @Transactional
    public ResumeBasicResponse upsertBasic(UUID resumeId, String secret, ResumeBasicCreateRequest req) {
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

        // 수정 경로: null = 변경 없음
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
    }


    //이력서 학력/경력/포트폴리오 링크 정보 UPDATE/INSERT
    @Transactional
    public void upsertAggregate(UUID resumeId, String secret, ResumeAggregateUpdateRequest req) {
        tempAuth.authenticate(resumeId, secret);

        if (req.core() != null) {
            var c = req.core();
            boolean hasAny = (c.templateId() != null) || (c.name() != null) || (c.phone() != null) || (c.email() != null);
            if (hasAny) throw new CustomException(ResumeErrorCode.INVALID_REQUEST);
        }

        Resume ref = resumeRepository.getReferenceById(resumeId);

        if (req.educations() != null) {
            educationRepository.deleteByResume_Id(resumeId);
            for (var d : req.educations()) educationRepository.save(mapEducation(ref, d));
        }
        if (req.experiences() != null) {
            experienceRepository.deleteByResume_Id(resumeId);
            for (var d : req.experiences()) experienceRepository.save(mapExperience(ref, d));
        }
        if (req.certificates() != null) {
            certificateRepository.deleteByResume_Id(resumeId);
            for (var d : req.certificates()) certificateRepository.save(mapCertificate(ref, d));
        }
        if (req.links() != null) {
            linkRepository.deleteByResume_Id(resumeId);
            for (var d : req.links()) linkRepository.save(mapLink(ref, d));
        }
    }

    //이력서 자격증 정보 UPDATE/INSERT upsertCertificates
    @Transactional
    public void upsertCertificates(UUID resumeId, String secret, List<ResumeCertificateUpsertRequest> certs) {
        tempAuth.authenticate(resumeId, secret);
        certificateRepository.deleteByResume_Id(resumeId);
        if(certs == null || certs.isEmpty()) return;

        Resume ref = resumeRepository.getReferenceById(resumeId);
        for (var d : certs) certificateRepository.save(mapCertificate(ref, d));
    }

    //이력서 자기소개서 답변 UPDATE/INSERT saveTemplateResponses
    @Transactional
    public CompanyTemplateResponseResponse saveTemplateResponses(UUID resumeId, String secret, CompanyTemplateResponseCreateRequest ctr){
        Resume resume = tempAuth.authenticate(resumeId, secret);

        List<CompanyTemplateResponse> saved = new ArrayList<>();
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
        return certificateRepository.findByIdAndResume_Id(certificateId, resumeId).isPresent();
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
                .label(d.label())
                .contents(null)
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