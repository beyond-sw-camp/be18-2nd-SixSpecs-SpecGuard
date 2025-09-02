package com.beyond.specguard.resume.service;

import com.beyond.specguard.resume.dto.experience.ResumeExperienceCreateRequest;
import com.beyond.specguard.resume.dto.experience.ResumeExperienceResponse;
import com.beyond.specguard.resume.dto.experience.ResumeExperienceUpdateRequest;
import com.beyond.specguard.resume.entity.core.Resume;
import com.beyond.specguard.resume.entity.core.ResumeExperience;
import com.beyond.specguard.resume.repository.ResumeExperienceRepository;
import com.beyond.specguard.resume.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ResumeExperienceService {

    private final ResumeRepository resumeRepository;
    private final ResumeExperienceRepository experienceRepository;

    @Transactional
    public ResumeExperienceResponse create(String resumeId, ResumeExperienceCreateRequest req) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 resumeId: " + resumeId));

        ResumeExperience saved = experienceRepository.save(
                ResumeExperience.builder()
                        .resume(resume)
                        .companyName(req.companyName())
                        .department(req.department())
                        .position(req.position())
                        .responsibilities(req.responsibilities())
                        .startDate(req.startDate())
                        .endDate(req.endDate())
                        .employmentStatus(req.employmentStatus())
                        .build()
        );
        return toResponse(saved);
    }

    // Read(list): 이력서에 등록된 모든 경력
    @Transactional(readOnly = true)
    public List<ResumeExperienceResponse> listByResumeId(String resumeId) {
        return experienceRepository.findByResumeId(resumeId)
                .stream().map(this::toResponse).toList();
    }

    // Update: 경력 1건 수정 (부분 업데이트: null이면 기존값 유지)
    @Transactional
    public ResumeExperienceResponse update(String experienceId, ResumeExperienceUpdateRequest req) {
        ResumeExperience cur = experienceRepository.findById(experienceId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 experienceId: " + experienceId));

        ResumeExperience updated = ResumeExperience.builder()
                .id(cur.getId())
                .resume(cur.getResume())
                .companyName(req.companyName() != null ? req.companyName() : cur.getCompanyName())
                .department(req.department() != null ? req.department() : cur.getDepartment())
                .position(req.position() != null ? req.position() : cur.getPosition())
                .responsibilities(req.responsibilities() != null ? req.responsibilities() : cur.getResponsibilities())
                .startDate(req.startDate() != null ? req.startDate() : cur.getStartDate())
                .endDate(req.endDate() != null ? req.endDate() : cur.getEndDate())
                .employmentStatus(req.employmentStatus() != null ? req.employmentStatus() : cur.getEmploymentStatus())
                .build();

        return toResponse(experienceRepository.save(updated));
    }

    // Delete: 경력 1건 삭제
    @Transactional
    public void delete(String experienceId) {
        if (!experienceRepository.existsById(experienceId)) {
            throw new IllegalArgumentException("존재하지 않는 experienceId: " + experienceId);
        }
        experienceRepository.deleteById(experienceId);
    }

    private ResumeExperienceResponse toResponse(ResumeExperience e) {
        return new ResumeExperienceResponse(
                e.getId(),
                e.getResume().getId(),
                e.getCompanyName(),
                e.getDepartment(),
                e.getPosition(),
                e.getResponsibilities(),
                e.getStartDate(),
                e.getEndDate(),
                e.getEmploymentStatus(),
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }
}
