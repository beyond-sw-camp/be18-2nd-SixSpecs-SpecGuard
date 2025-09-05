package com.beyond.specguard.resume.service;

import com.beyond.specguard.resume.dto.request.*;
import com.beyond.specguard.resume.dto.response.CompanyTemplateResponseResponse;
import com.beyond.specguard.resume.dto.response.ResumeBasicResponse;
import com.beyond.specguard.resume.dto.response.ResumeResponse;
import com.beyond.specguard.resume.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

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

    //생성 / 조회 / 코어수정 / 상태변경 / 삭제


    @Transactional
    public ResumeResponse create(ResumeCreateRequest req) {
    }

    @Transactional(readOnly = true)
    public ResumeResponse get(UUID resumeId) {
    }

    @Transactional
    public ResumeResponse updateCore(UUID resumeId, ResumeUpdateRequest req) {
    }

    @Transactional
    public ResumeResponse updateStatus(UUID resumeId, ResumeStatusUpdateRequest req) {
    }

    @Transactional
    public void delete(UUID resumeId) {
    }

    //기본 정보
    @Transactional
    public ResumeBasicResponse upsertBasic(UUID resumeId, ResumeBasicCreateRequest req) {

    }

   //집계
    @Transactional
    public void upsertAggregate(UUID resumeId, ResumeAggregateUpdateRequest req) {

    }

    //답변 저장
    @Transactional
    public CompanyTemplateResponseResponse saveTemplateResponses(UUID resumeId,

    }

    // dto -> entity 매핑

    // entity -> dto 변환

