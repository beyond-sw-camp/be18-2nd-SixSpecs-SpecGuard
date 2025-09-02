package com.beyond.specguard.resume.controller;

import com.beyond.specguard.resume.ApiResponse;
import com.beyond.specguard.resume.dto.experience.ResumeExperienceCreateRequest;
import com.beyond.specguard.resume.dto.experience.ResumeExperienceResponse;
import com.beyond.specguard.resume.dto.experience.ResumeExperienceUpdateRequest;
import com.beyond.specguard.resume.service.ResumeExperienceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/resumes")
public class ResumeExperienceController {

    private final ResumeExperienceService service;

    // [POST] /api/v1/resumes/{resumeId}/experiences
    @PostMapping("/{resumeId}/experiences")
    public ResponseEntity<ApiResponse<ResumeExperienceResponse>> create(
            @PathVariable String resumeId,
            @RequestBody ResumeExperienceCreateRequest req
    ) {
        var data = service.create(resumeId, req);
        return ResponseEntity.ok(ApiResponse.ok("경력 생성 완료", data));
    }

    // [GET] /api/v1/resumes/{resumeId}/experiences
    @GetMapping("/{resumeId}/experiences")
    public ResponseEntity<ApiResponse<List<ResumeExperienceResponse>>> list(
            @PathVariable String resumeId
    ) {
        var data = service.listByResumeId(resumeId);
        return ResponseEntity.ok(ApiResponse.ok("경력 목록 조회", data));
    }

    // [PUT] /api/v1/resumes/experiences/{experienceId}
    @PutMapping("/experiences/{experienceId}")
    public ResponseEntity<ApiResponse<ResumeExperienceResponse>> update(
            @PathVariable String experienceId,
            @RequestBody ResumeExperienceUpdateRequest req
    ) {
        var data = service.update(experienceId, req);
        return ResponseEntity.ok(ApiResponse.ok("경력 수정 완료", data));
    }

    // [DELETE] /api/v1/resumes/experiences/{experienceId}
    @DeleteMapping("/experiences/{experienceId}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String experienceId) {
        service.delete(experienceId);
        return ResponseEntity.ok(ApiResponse.ok("경력 삭제 완료", null));
    }
}