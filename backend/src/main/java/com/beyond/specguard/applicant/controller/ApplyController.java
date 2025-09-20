package com.beyond.specguard.applicant.controller;

import com.beyond.specguard.companytemplate.model.dto.response.CompanyTemplateListResponseDto;
import com.beyond.specguard.companytemplate.model.service.CompanyTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/resumes")
public class ApplyController {
    private final CompanyTemplateService companyTemplateService;

    @GetMapping("/companies/{companySlug}/templates")
    public ResponseEntity<CompanyTemplateListResponseDto> getTemplates(
            @PathVariable String companySlug
    ) {
        CompanyTemplateListResponseDto response = companyTemplateService.getTemplates(companySlug);

        return ResponseEntity.ok(response);
    }
}
