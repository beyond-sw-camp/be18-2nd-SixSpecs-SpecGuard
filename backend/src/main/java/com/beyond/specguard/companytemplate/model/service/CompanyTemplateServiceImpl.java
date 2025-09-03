package com.beyond.specguard.companytemplate.model.service;

import com.beyond.specguard.common.exception.CustomException;
import com.beyond.specguard.companytemplate.exception.ErrorCode.CompanyTemplateErrorCode;
import com.beyond.specguard.companytemplate.model.entity.CompanyTemplate;
import com.beyond.specguard.companytemplate.model.repository.CompanyTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompanyTemplateServiceImpl implements CompanyTemplateService {
    private final CompanyTemplateRepository companyTemplateRepository;

    @Override
    public CompanyTemplate getCompanyTemplate(UUID templateId) {
        return companyTemplateRepository.findById(templateId)
                .orElseThrow(() -> new CustomException(CompanyTemplateErrorCode.TEMPLATE_NOT_FOUND));
    }

    @Override
    public CompanyTemplate createTemplate(CompanyTemplate template) {
        CompanyTemplate companyTemplate = template;
        if (template.getId() == null) {
            companyTemplate = companyTemplateRepository.save(template);
        } else {
            if (companyTemplateRepository.existsById(template.getId())) {
                companyTemplate = companyTemplateRepository.save(template);
            } else {
                throw new CustomException(CompanyTemplateErrorCode.TEMPLATE_NOT_FOUND);
            }
        }
        return companyTemplate;
    }
}
