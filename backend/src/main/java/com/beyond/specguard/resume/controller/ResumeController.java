package com.beyond.specguard.resume.controller;

import com.beyond.specguard.resume.dto.resume.request.ResumeCreateRequest;
import com.beyond.specguard.resume.dto.resume.request.ResumeStatusUpdateRequest;
import com.beyond.specguard.resume.dto.resume.request.ResumeUpdateRequest;
import com.beyond.specguard.resume.dto.resume.response.ResumeResponse;
import com.beyond.specguard.resume.service.ResumeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



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

    @GetMapping("/{id}")
    public ResumeResponse get(@PathVariable String id) {
        return resumeService.get(id);
    }



    @PatchMapping("/{id}")
    public ResumeResponse update(
            @PathVariable String id,
            @Valid @RequestBody ResumeUpdateRequest req) {
        return resumeService.update(id, req);
    }

    @PatchMapping("{id}/status")
    public ResumeResponse updateStatus(
            @PathVariable String id,
            @Valid @RequestBody ResumeStatusUpdateRequest req){
        return resumeService.updateStatus(id, req);

    }


    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        resumeService.delete(id);
    }
}
