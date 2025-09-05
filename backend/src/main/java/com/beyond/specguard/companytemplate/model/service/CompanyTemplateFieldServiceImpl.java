package com.beyond.specguard.companytemplate.model.service;

import com.beyond.specguard.companytemplate.model.entity.CompanyTemplateField;
import com.beyond.specguard.companytemplate.model.repository.CompanyTemplateFieldRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompanyTemplateFieldServiceImpl implements CompanyTemplateFieldService {

    private final CompanyTemplateFieldRepository companyTemplateFieldRepository;

    @Override
    @Transactional
    public List<CompanyTemplateField> createField(List<CompanyTemplateField> companyTemplateFields) {
        return companyTemplateFieldRepository.saveAll(companyTemplateFields);
    }

    @Override
    public List<CompanyTemplateField> getFields(UUID templateId) {
        return companyTemplateFieldRepository.findAllByTemplate_Id(templateId);
    }

    @Override
    @Transactional
    public void deleteField(UUID templateId) {
        companyTemplateFieldRepository.deleteByTemplate_Id(templateId);
    }
}
