package com.beyond.specguard.companytemplate.controller;

import com.beyond.specguard.companytemplate.model.dto.CompanyTemplateBasicRequestDto;
import com.beyond.specguard.companytemplate.model.dto.CompanyTemplateBasicResponseDto;
import com.beyond.specguard.companytemplate.model.dto.CompanyTemplateDetailRequestDto;
import com.beyond.specguard.companytemplate.model.dto.CompanyTemplateDetailResponseDto;
import com.beyond.specguard.companytemplate.model.entity.CompanyTemplate;
import com.beyond.specguard.companytemplate.model.entity.CompanyTemplateField;
import com.beyond.specguard.companytemplate.model.service.CompanyTemplateFieldService;
import com.beyond.specguard.companytemplate.model.service.CompanyTemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/company-template")
@RequiredArgsConstructor
public class CompanyTemplateController {

    private final CompanyTemplateService companyTemplateService;
    private final CompanyTemplateFieldService companyTemplateFieldService;

    @PostMapping("/basic")
    public ResponseEntity<CompanyTemplateBasicResponseDto> createTemplate(
            @Valid @RequestBody CompanyTemplateBasicRequestDto basicRequestDto
    ) {
        CompanyTemplate companyTemplate = basicRequestDto.toEntity();

        log.debug(companyTemplate.toString());

        CompanyTemplate saved = companyTemplateService.createTemplate(companyTemplate);

        log.debug(saved.toString());

        return new ResponseEntity<>(
                CompanyTemplateBasicResponseDto.toDto(saved),
                HttpStatus.CREATED
        );
    }

    @PostMapping("/detail")
    public ResponseEntity<CompanyTemplateDetailResponseDto> createDetailTemplate(
        @Valid @RequestBody CompanyTemplateDetailRequestDto requestDto
    ) {
        CompanyTemplate companyTemplate = companyTemplateService.getCompanyTemplate(requestDto.getTemplateId());

        log.debug(companyTemplate.toString());

        companyTemplate.update(requestDto);

        log.debug(companyTemplate.toString());

        CompanyTemplate saved = companyTemplateService.createTemplate(companyTemplate);

        List< CompanyTemplateField> companyTemplateFields =
                companyTemplateFieldService.createField(
                        requestDto.getFields()
                                .stream()
                                .map(field -> field.toEntity(companyTemplate))
                                .toList()
                );

        return new ResponseEntity<>(
                CompanyTemplateDetailResponseDto.toDto(saved, companyTemplateFields),
                HttpStatus.CREATED
        );
    }
}
