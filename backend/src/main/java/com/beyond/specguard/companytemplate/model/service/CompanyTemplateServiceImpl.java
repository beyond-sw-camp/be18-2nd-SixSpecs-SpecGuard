package com.beyond.specguard.companytemplate.model.service;

import com.beyond.specguard.common.exception.CustomException;
import com.beyond.specguard.companytemplate.exception.ErrorCode.CompanyTemplateErrorCode;
import com.beyond.specguard.companytemplate.model.entity.CompanyTemplate;
import com.beyond.specguard.companytemplate.model.repository.CompanyTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional
    public void deleteTemplate(UUID templateId) {
        companyTemplateRepository.deleteById(templateId);
    }

    @Override
    @Transactional
    public CompanyTemplate updateTemplate(CompanyTemplate companyTemplate) {
        return companyTemplateRepository.save(companyTemplate);
    }

    @Override
    @Transactional
    public CompanyTemplate createTemplate(CompanyTemplate template) {
        CompanyTemplate companyTemplate = template;

        if (template.getId() == null) {
            // 신규 생성 케이스: ID가 없으므로 save() → INSERT 발생
            companyTemplate = companyTemplateRepository.save(template);
        } else {
            // 업데이트 케이스: ID가 존재하는 경우
            if (companyTemplateRepository.existsById(template.getId())) {
                template.setActive(true);
                companyTemplate = companyTemplateRepository.save(template);
            } else {
                // ID는 있는데 DB에 존재하지 않는 경우 → 예외 처리
                throw new CustomException(CompanyTemplateErrorCode.TEMPLATE_NOT_FOUND);
            }
        }
        return companyTemplate;
    }
}
