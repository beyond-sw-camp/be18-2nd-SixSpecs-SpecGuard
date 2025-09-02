package com.beyond.specguard.resume.controller;

import com.beyond.specguard.resume.dto.education.ResumeEducationCreateRequest;
import com.beyond.specguard.resume.dto.education.ResumeEducationResponse;
import com.beyond.specguard.resume.dto.education.ResumeEducationUpdateRequest;
import com.beyond.specguard.resume.service.ResumeEducationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/resumes")
public class ResumeEducationController {

    private final ResumeEducationService eduService;

    @GetMapping("/{resumeId}/educations")
    public List<ResumeEducationResponse> list(@PathVariable String resumeId){
        return eduService.listByResume(resumeId);
    }

    @PostMapping("/{resumeId}/educations")
    @ResponseStatus(HttpStatus.CREATED)
    public ResumeEducationResponse create(@PathVariable String resumeId,
                                          @Valid @RequestBody ResumeEducationCreateRequest req) {


        var fixed = new ResumeEducationCreateRequest(
                resumeId,
                req.schoolType(),
                req.schoolName(),
                req.major(),
                req.degree(),
                req.admissionType(),
                req.graduationStatus(),
                req.gpa(),
                req.maxGpa(),
                req.startDate(),
                req.endDate()
        );
        return eduService.create(fixed);
    }

    @PatchMapping("/educations/{educationId}")
    public ResumeEducationResponse update(@PathVariable String educationId,
                                          @RequestBody ResumeEducationUpdateRequest req) {
        return eduService.update(educationId, req);
    }

    @DeleteMapping("/educations/{educationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String educationId) {
        eduService.delete(educationId);
    }


}
