package com.beyond.specguard.resume.controller;

import com.beyond.specguard.resume.dto.basic.ResumeBasicCreateRequest;
import com.beyond.specguard.resume.dto.basic.ResumeBasicResponse;
import com.beyond.specguard.resume.dto.basic.ResumeBasicUpdateRequest;
import com.beyond.specguard.resume.service.ResumeBasicService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/resumes/{resumeId}/basic")
public class ResumeBasicController {
    private final ResumeBasicService resumeBasicService;

    // POST /api/v1/resumes/{resumeId}/basic
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResumeBasicResponse create(@PathVariable String resumeId,
                                                      @RequestBody @Valid ResumeBasicCreateRequest req) {

        var fixed = new ResumeBasicCreateRequest(
                resumeId,
                req.englishName(),
                req.gender(),
                req.birthDate(),
                req.nationality(),
                req.applyField(),
                req.profileImageUrl(),
                req.address(),
                req.specialty(),
                req.hobbies()
        );
        return resumeBasicService.create(fixed);
    }

    // GET /api/v1/resumes/{resumeId}/basic
    @GetMapping
    public ResumeBasicResponse get(@PathVariable String resumeId) {
        return resumeBasicService.getByResumeId(resumeId);
    }

    // PATCH /api/v1/resumes/{resumeId}/basic
    @PatchMapping
    public ResumeBasicResponse update(@PathVariable String resumeId,
                                      @RequestBody @Valid ResumeBasicUpdateRequest req) {
        return resumeBasicService.update(resumeId, req);
    }

}
