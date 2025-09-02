package com.beyond.specguard.companytemplate.controller;

import com.beyond.specguard.companytemplate.model.dto.CompanyTemplateBasicRequestDto;
import com.beyond.specguard.companytemplate.model.entity.CompanyTemplate;
import com.beyond.specguard.companytemplate.model.service.CompanyTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/company-templates")
@RequiredArgsConstructor
public class CompanyTemplateController {

    private final CompanyTemplateService companyTemplateService;

    @PostMapping("/basic")
    public ResponseEntity<CompanyTemplate> createTemplate(
            @RequestBody CompanyTemplateBasicRequestDto basicRequestDto
    ) {
        CompanyTemplate companyTemplate = basicRequestDto.toDto();

        log.debug(companyTemplate.toString());
        CompanyTemplate saved = companyTemplateService.createTemplate(companyTemplate);

        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }
}
