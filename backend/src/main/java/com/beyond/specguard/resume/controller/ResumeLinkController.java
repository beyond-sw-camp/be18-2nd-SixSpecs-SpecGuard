package com.beyond.specguard.resume.controller;

import com.beyond.specguard.resume.dto.link.ResumeLinkCreateRequest;
import com.beyond.specguard.resume.dto.link.ResumeLinkResponse;
import com.beyond.specguard.resume.dto.link.ResumeLinkUpdateRequest;
import com.beyond.specguard.resume.service.ResumeLinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/resumes/{resumeId}/links")
public class ResumeLinkController {

    private final ResumeLinkService resumeLinkService;

    /** 생성 POST /api/v1/resumes/{resumeId}/links */
    @PostMapping
    public ResponseEntity<ResumeLinkResponse> create(
            @PathVariable String resumeId,
            @RequestBody ResumeLinkCreateRequest req
    ) {
        ResumeLinkResponse res = resumeLinkService.create(resumeId, req);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    /** 목록 GET /api/v1/resumes/{resumeId}/links */
    @GetMapping
    public ResponseEntity<List<ResumeLinkResponse>> list(@PathVariable String resumeId) {
        return ResponseEntity.ok(resumeLinkService.list(resumeId));
    }

    /** 단건 GET /api/v1/resumes/{resumeId}/links/{linkId} */
    @GetMapping("/{linkId}")
    public ResponseEntity<ResumeLinkResponse> get(
            @PathVariable String resumeId,
            @PathVariable String linkId
    ) {
        return ResponseEntity.ok(resumeLinkService.get(resumeId, linkId));
    }

    /** 수정 PATCH /api/v1/resumes/{resumeId}/links/{linkId} */
    @PatchMapping("/{linkId}")
    public ResponseEntity<ResumeLinkResponse> update(
            @PathVariable String resumeId,
            @PathVariable String linkId,
            @RequestBody ResumeLinkUpdateRequest req
    ) {
        return ResponseEntity.ok(resumeLinkService.update(resumeId, linkId, req));
    }

    /** 삭제 DELETE /api/v1/resumes/{resumeId}/links/{linkId} */
    @DeleteMapping("/{linkId}")
    public ResponseEntity<Void> delete(
            @PathVariable String resumeId,
            @PathVariable String linkId
    ) {
        resumeLinkService.delete(resumeId, linkId);
        return ResponseEntity.noContent().build();
    }
}
