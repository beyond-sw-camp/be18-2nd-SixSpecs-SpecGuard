package com.beyond.specguard.resume.service;

import com.beyond.specguard.resume.dto.basic.ResumeBasicCreateRequest;
import com.beyond.specguard.resume.dto.basic.ResumeBasicResponse;
import com.beyond.specguard.resume.dto.basic.ResumeBasicUpdateRequest;
import com.beyond.specguard.resume.entity.core.Resume;
import com.beyond.specguard.resume.entity.core.ResumeBasic;
import com.beyond.specguard.resume.repository.ResumeBasicRepository;
import com.beyond.specguard.resume.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ResumeBasicService {

    private final ResumeRepository resumeRepository;
    private final ResumeBasicRepository resumeBasicRepository;

    //생성
    @Transactional
    public ResumeBasicResponse create(ResumeBasicCreateRequest req) {
        Resume resume = resumeRepository.findById(req.resumeId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 resume id"));

        if (resumeBasicRepository.existsByResumeId(req.resumeId())) {
            throw new IllegalStateException("기본 정보는 이미 존재합니다. 수정 API를 사용하세요.");
        }


        ResumeBasic saved = resumeBasicRepository.save(
                ResumeBasic.builder()
                        .resume(resume)
                        .englishName(req.englishName())
                        .gender(req.gender())
                        .birthDate(req.birthDate())
                        .nationality(req.nationality())
                        .applyField(req.applyField())
                        .profileImageUrl(req.profileImageUrl())
                        .address(req.address())
                        .specialty(req.specialty())
                        .hobbies(req.hobbies())
                        .build()
        );

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ResumeBasicResponse getByResumeId(String resumeId) {
        ResumeBasic basic = resumeBasicRepository.findByResumeId(resumeId)
                .orElseThrow(() -> new IllegalArgumentException("기본정보가 없습니다."));
        return toResponse(basic);
    }

    @Transactional
    public ResumeBasicResponse update(String resumeId, ResumeBasicUpdateRequest req) {

        ResumeBasic basic = resumeBasicRepository.findByResumeId(resumeId)
                .orElseThrow(() -> new IllegalArgumentException("기본정보가 없습니다."));


        ResumeBasic updated = ResumeBasic.builder()
                .id(basic.getId())
                .resume(basic.getResume())
                .englishName(req.englishName() != null ? req.englishName() : basic.getEnglishName())
                .gender     (req.gender()      != null ? req.gender()      : basic.getGender())
                .birthDate  (req.birthDate()   != null ? req.birthDate()   : basic.getBirthDate())
                .nationality(req.nationality() != null ? req.nationality() : basic.getNationality())
                .applyField (req.applyField()  != null ? req.applyField()  : basic.getApplyField())
                .profileImageUrl(req.profileImageUrl() != null ? req.profileImageUrl() : basic.getProfileImageUrl())
                .address    (req.address()     != null ? req.address()     : basic.getAddress())
                .specialty  (req.specialty()   != null ? req.specialty()   : basic.getSpecialty())
                .hobbies    (req.hobbies()     != null ? req.hobbies()     : basic.getHobbies())
                .build();


        ResumeBasic saved = resumeBasicRepository.save(updated);
        return toResponse(saved);
    }

    private ResumeBasicResponse toResponse(ResumeBasic e) {
        return new ResumeBasicResponse(
                e.getId(),
                e.getResume().getId(),
                e.getEnglishName(),
                e.getGender(),
                e.getBirthDate(),
                e.getNationality(),
                e.getAddress(),
                e.getSpecialty(),
                e.getHobbies(),
                e.getApplyField(),
                e.getProfileImageUrl(),
                e.getCreatedAt(),
                e.getUpdatedAt()

        );
    }
}
