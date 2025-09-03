package com.beyond.specguard.resume.service;

import com.beyond.specguard.resume.dto.education.ResumeEducationCreateRequest;
import com.beyond.specguard.resume.dto.education.ResumeEducationResponse;
import com.beyond.specguard.resume.dto.education.ResumeEducationUpdateRequest;
import com.beyond.specguard.resume.entity.core.Resume;
import com.beyond.specguard.resume.entity.core.ResumeEducation;
import com.beyond.specguard.resume.repository.ResumeEducationRepository;
import com.beyond.specguard.resume.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
public class ResumeEducationService {

    private final ResumeRepository resumeRepository;
    private final ResumeEducationRepository resumeEducationRepository;


    @Transactional
    public ResumeEducationResponse create(ResumeEducationCreateRequest req) {
        Resume resume = resumeRepository.findById(req.resumeId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 resume id"));



        ResumeEducation saved = resumeEducationRepository.save(
                ResumeEducation.builder()
                        .resume(resume)
                        .schoolName(req.schoolName())
                        .schoolType(req.schoolType())
                        .major(req.major())
                        .graduationStatus(req.graduationStatus())
                        .degree(req.degree())
                        .admissionType(req.admissionType())
                        .gpa(req.gpa())
                        .maxGpa(req.maxGpa())
                        .startDate(req.startDate())
                        .endDate(req.endDate())
                        .build()
        );
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ResumeEducationResponse> listByResume(String resumeId) {
        return resumeEducationRepository.findByResumeId(resumeId).stream().map(this::toResponse).toList();
    }




    @Transactional
    public ResumeEducationResponse update(String educationId, ResumeEducationUpdateRequest req) {
        ResumeEducation cur = resumeEducationRepository.findById(educationId)
                .orElseThrow(() -> new IllegalArgumentException("EDUCATION_NOT_FOUND"));

        ResumeEducation updated = ResumeEducation.builder()

                .resume(cur.getResume())
                .schoolName(req.schoolName() != null ? req.schoolName() : cur.getSchoolName())
                .major     (req.major()      != null ? req.major()      : cur.getMajor())
                .graduationStatus(req.graduationStatus()!=null?req.graduationStatus():cur.getGraduationStatus())
                .degree    (req.degree()     != null ? req.degree()     : cur.getDegree())
                .admissionType(req.admissionType()!=null?req.admissionType():cur.getAdmissionType())
                .gpa       (req.gpa()        != null ? req.gpa()        : cur.getGpa())
                .maxGpa    (req.maxGpa()     != null ? req.maxGpa()     : cur.getMaxGpa())
                .startDate (req.startDate()  != null ? req.startDate()  : cur.getStartDate())
                .endDate   (req.endDate()    != null ? req.endDate()    : cur.getEndDate())
                .build();

        return toResponse(resumeEducationRepository.save(updated));
    }

    @Transactional
    public void delete(String educationId) {
        if (!resumeEducationRepository.existsById(educationId)) {
            throw new IllegalArgumentException("EDUCATION_NOT_FOUND");
        }
        resumeEducationRepository.deleteById(educationId);
    }

    private ResumeEducationResponse toResponse(ResumeEducation e) {
        return new ResumeEducationResponse(
                e.getId(),
                e.getResume().getId(),
                e.getSchoolType(),
                e.getSchoolName(),
                e.getMajor(),
                e.getDegree(),
                e.getGraduationStatus(),
                e.getAdmissionType(),
                e.getGpa(),
                e.getMaxGpa(),
                e.getStartDate(),
                e.getEndDate(),
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }






}
