package com.beyond.specguard.resume.service;

import com.beyond.specguard.resume.dto.request.*;
import com.beyond.specguard.resume.dto.response.CompanyTemplateResponseResponse;
import com.beyond.specguard.resume.dto.response.ResumeBasicResponse;
import com.beyond.specguard.resume.dto.response.ResumeResponse;
import com.beyond.specguard.resume.entity.common.enums.ResumeStatus;
import com.beyond.specguard.resume.entity.core.*;
import com.beyond.specguard.resume.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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


    //이력서 생성에서 create
    @Transactional
    public ResumeResponse create(ResumeCreateRequest req) {
        Resume saved = resumeRepository.save(
                Resume.builder()
                        .templateId(req.templateId())
                        .status(ResumeStatus.DRAFT)
                        .name(req.name())
                        .phone(req.phone())
                        .email(req.email())
                        .passwordHash(req.passwordHash())
                        .build()
        );
        return toResumeResponse(saved);
    }

    //지원서 단건 조회에서 get
    @Transactional(readOnly = true)
    public ResumeResponse get(UUID resumeId) {
        Resume r = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new IllegalArgumentException("이력서를 찾을 수 없습니다"));
        return toResumeResponse(r);
    }

    //지원서 목록 조회에서 list
    @Transactional(readOnly = true)
    public Page<ResumeResponse> list(Pageable pageable) {
        return resumeRepository.findAll(pageable).map(this::toResumeResponse);
    }

    //이력서 기본 정보 UPDATE/INSERT에서 upsertBasic
    @Transactional
    public ResumeBasicResponse upsertBasic(UUID resumeId, ResumeBasicCreateRequest req) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new IllegalArgumentException("이력서를 찾을 수 없습니다:" + resumeId));

        Optional<ResumeBasic> opt = basicRepository.findByResume_Id(resumeId);
        ResumeBasic basic = opt.orElseGet(() -> basicRepository.save(
                ResumeBasic.builder()
                        .resume(resume)
                        .englishName(req.englishName())
                        .gender(req.gender())
                        .birthDate(req.birthDate())
                        .nationality(req.nationality())
                        .applyField(req.applyField())
                        .profileImageUrl(req.profileImage()) // 현재는 URL 문자열
                        .address(req.address())
                        .specialty(req.specialty())
                        .hobbies(req.hobbies())
                        .build()
        ));

        // 수정 경로: null = 변경 없음
        if (opt.isPresent()) {
            if (req.englishName() != null) basicChangeEnglishName(basic, req.englishName());
            if (req.gender() != null)      basicChangeGender(basic, req.gender());
            if (req.birthDate() != null)   basicChangeBirthDate(basic, req.birthDate());
            if (req.nationality() != null) basicChangeNationality(basic, req.nationality());
            if (req.applyField() != null)  basicChangeApplyField(basic, req.applyField());
            if (req.address() != null)     basicChangeAddress(basic, req.address());
            if (req.specialty() != null)   basicChangeSpecialty(basic, req.specialty());
            if (req.hobbies() != null)     basicChangeHobbies(basic, req.hobbies());
            if (req.profileImage() != null) basicChangeProfileImageUrl(basic, req.profileImage());
        }
        return toBasicResponse(basic, resumeId);
    }

    //이력서 학력/경력/포트폴리오 링크 정보 UPDATE/INSERT
    @Transactional
    public void upsertAggregate(UUID resumeId, ResumeAggregateUpdateRequest req) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() ->new IllegalArgumentException("이력서를 찾을 수 없습니다: "+ resumeId));

        //1) 코어 변경
        if(req.core() != null){
            ResumeCorePatch c = req.core();
            if(c.templateId() != null){
                resume.changeTemplateId(c.templateId());
            }
            if(c.name() != null){
                resume.changeName(c.name());
            }
            if(c.phone() != null){
                resume.changePhone(c.phone());
            }
            if(c.email() != null){
                resume.changeEmail(c.email());
            }
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
    public void upsertCertificates(UUID resumeId, List<ResumeCertificateUpsertRequest> certs) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new IllegalArgumentException("이력서를 찾을 수 없습니다 : "+ resumeId));
        certificateRepository.deleteByResume_Id(resumeId);
        if(certs == null || certs.isEmpty()) return;

        Resume ref = resumeRepository.getReferenceById(resumeId);
        for (var d : certs) {
            certificateRepository.save(mapCertificate(ref, d));
        }
    }

    //이력서 자기소개서 답변 UPDATE/INSERT saveTemplateResponses
    @Transactional
    public CompanyTemplateResponseResponse saveTemplateResponses(UUID resumeId,  CompanyTemplateResponseCreateRequest ctr){
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(()-> new IllegalArgumentException("이력서를 찾을 수 없습니다 : "+resumeId));

        List<CompanyTemplateResponse> saved = new ArrayList<>();
        for (var i : ctr.responses()) {
            var opt = templateResponseRepository.findByResume_IdAndFieldId(resumeId, i.fieldId());
            CompanyTemplateResponse e;
            if (opt.isPresent()) {
                e = opt.get();
                e.changeAnswer(i.answer());
            } else {
                e = CompanyTemplateResponse.builder()
                        .resume(resume)
                        .fieldId(i.fieldId())
                        .answer(i.answer())
                        .build();
            }
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
    public boolean verifyCertificate(UUID resumeId, UUID certificateId) {
        return certificateRepository.findByIdAndResume_Id(certificateId, resumeId).isPresent();
    }


    //내부 유틸
    private static void set(Object target, String fieldName, Object value) {
        Field f = ReflectionUtils.findField(target.getClass(), fieldName);
        if (f == null) throw new IllegalStateException("필드 없음: " + fieldName);
        ReflectionUtils.makeAccessible(f);
        ReflectionUtils.setField(f, target, value);
    }

    // 목적형 변경(엔티티에 메서드가 없다면 서비스에서 래핑)
    private void basicChangeEnglishName(ResumeBasic b, String v) { set(b, "englishName", v); }
    private void basicChangeGender(ResumeBasic b, Object v)      { set(b, "gender", v); }
    private void basicChangeBirthDate(ResumeBasic b, Object v)   { set(b, "birthDate", v); }
    private void basicChangeNationality(ResumeBasic b, String v) { set(b, "nationality", v); }
    private void basicChangeApplyField(ResumeBasic b, String v)  { set(b, "applyField", v); }
    private void basicChangeAddress(ResumeBasic b, String v)     { set(b, "address", v); }
    private void basicChangeSpecialty(ResumeBasic b, String v)   { set(b, "specialty", v); }
    private void basicChangeHobbies(ResumeBasic b, String v)     { set(b, "hobbies", v); }
    private void basicChangeProfileImageUrl(ResumeBasic b, String v) { set(b, "profileImageUrl", v); }







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



