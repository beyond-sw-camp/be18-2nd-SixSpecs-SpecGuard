package com.beyond.specguard.companytemplate.model.service;

import com.beyond.specguard.companytemplate.model.entity.CompanyTemplate;
import com.beyond.specguard.companytemplate.model.repository.CompanyTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompanyTemplateServiceImpl implements CompanyTemplateService {
    private final CompanyTemplateRepository companyTemplateRepository;
    @Override
    public CompanyTemplate createTemplate(CompanyTemplate template) {
        CompanyTemplate companyTemplate = template;
        if (!template.isActive()) {
            companyTemplate = companyTemplateRepository.save(template);
        }
        return companyTemplate;
    }
}
