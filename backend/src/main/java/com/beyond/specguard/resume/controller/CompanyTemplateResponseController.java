package com.beyond.specguard.resume.controller;

import com.beyond.specguard.resume.dto.templateResponse.CompanyTemplateResponseCreateRequest;
import com.beyond.specguard.resume.dto.templateResponse.CompanyTemplateResponseResponse;
import com.beyond.specguard.resume.dto.templateResponse.CompanyTemplateResponseUpdateRequest;
import com.beyond.specguard.resume.service.CompanyTemplateResponseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/resumes/{resumeId}/responses")
public class CompanyTemplateResponseController {

    private final CompanyTemplateResponseService service;

    /** 생성 */
    @PostMapping
    public ResponseEntity<CompanyTemplateResponseResponse> create(
            @PathVariable String resumeId,
            @RequestBody CompanyTemplateResponseCreateRequest req
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(resumeId, req));
    }

    /** 목록 조회 */
    @GetMapping
    public ResponseEntity<List<CompanyTemplateResponseResponse>> list(@PathVariable String resumeId) {
        return ResponseEntity.ok(service.list(resumeId));
    }

    /** 단건 조회 */
    @GetMapping("/{respId}")
    public ResponseEntity<CompanyTemplateResponseResponse> get(
            @PathVariable String resumeId,
            @PathVariable String respId
    ) {
        return ResponseEntity.ok(service.get(resumeId, respId));
    }

    /** 수정 */
    @PatchMapping("/{respId}")
    public ResponseEntity<CompanyTemplateResponseResponse> update(
            @PathVariable String resumeId,
            @PathVariable String respId,
            @RequestBody CompanyTemplateResponseUpdateRequest req
    ) {
        return ResponseEntity.ok(service.update(resumeId, respId, req));
    }

    /** 삭제 */
    @DeleteMapping("/{respId}")
    public ResponseEntity<Void> delete(
            @PathVariable String resumeId,
            @PathVariable String respId
    ) {
        service.delete(resumeId, respId);
        return ResponseEntity.noContent().build();
    }
}