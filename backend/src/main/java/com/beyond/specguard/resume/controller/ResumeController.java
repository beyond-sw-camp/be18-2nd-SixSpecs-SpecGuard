package com.beyond.specguard.resume.controller;

import com.beyond.specguard.resume.dto.request.ResumeCreateRequest;
import com.beyond.specguard.resume.dto.request.ResumeStatusUpdateRequest;
import com.beyond.specguard.resume.dto.request.ResumeUpdateRequest;
import com.beyond.specguard.resume.dto.response.ResumeResponse;
import com.beyond.specguard.resume.service.ResumeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/resumes")
public class ResumeController {

    private final ResumeService resumeService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResumeResponse create(@Valid @RequestBody ResumeCreateRequest req) {
        return resumeService.create(req);
    }

    @GetMapping("/{resumeId}")
    public ResumeResponse get(@PathVariable UUID id) {
        return resumeService.get(id);
    }

    @PatchMapping("/{resumeId}")
    public ResumeResponse update(@PathVariable UUID id,
                                 @Valid @RequestBody ResumeUpdateRequest req) {
        return resumeService.update(id, req);
    }

    @PatchMapping("/{resumeId}/status")
    public ResumeResponse updateStatus(@PathVariable UUID id,
                                       @Valid @RequestBody ResumeStatusUpdateRequest req) {
        return resumeService.updateStatus(id, req);
    }

    @DeleteMapping("/{resumeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        resumeService.delete(id);
    }
}
