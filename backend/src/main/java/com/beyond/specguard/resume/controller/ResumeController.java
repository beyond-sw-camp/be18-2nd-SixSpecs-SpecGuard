package com.beyond.specguard.resume.controller;

import com.beyond.specguard.resume.dto.request.ResumeCreateRequest;
import com.beyond.specguard.resume.dto.request.ResumeUpdateRequest;
import com.beyond.specguard.resume.dto.response.ResumeListItem;
import com.beyond.specguard.resume.dto.response.ResumeResponse;
import com.beyond.specguard.resume.service.ResumeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/resumes")
public class ResumeController {

    private final ResumeService resumeService;

    // 이력서 생성
    @PostMapping
    public ResponseEntity<ResumeResponse> create(
            @Valid @RequestBody ResumeCreateRequest req) {
        var created = resumeService.create(req);
        return ResponseEntity
                .created(URI.create("/api/v1/resumes/" + created.id()))
                .body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResumeResponse> get(@PathVariable String id) {
        return ResponseEntity.ok(resumeService.get(id));
    }

    // 이력서 목록 조회(요약)
    @GetMapping
    public ResponseEntity<List<ResumeListItem>> list() {
        return ResponseEntity.ok(resumeService.list());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ResumeResponse> update(
            @PathVariable String id,
            @Valid @RequestBody ResumeUpdateRequest req) {
        return ResponseEntity.ok(resumeService.update(id, req));
    }

    // 이력서 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        resumeService.delete(id);
        return ResponseEntity.noContent().build();                
    }
}
