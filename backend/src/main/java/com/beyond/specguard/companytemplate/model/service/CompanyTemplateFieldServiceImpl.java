package com.beyond.specguard.companytemplate.model.service;

import com.beyond.specguard.companytemplate.model.dto.command.CreateCompanyTemplateFieldCommand;
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
    @Transactional(readOnly = true)
    public List<CompanyTemplateField> getFields(UUID templateId) {
        return companyTemplateFieldRepository.findAllByTemplate_Id(templateId);
    }

    @Override
    @Transactional
    public void deleteFields(UUID templateId) {
        companyTemplateFieldRepository.deleteAllByTemplate_Id(templateId);
    }

    @Override
    @Transactional
    public List<CompanyTemplateField> updateFields(List<CompanyTemplateField> companyTemplateFields) {
        return companyTemplateFieldRepository.saveAll(companyTemplateFields);
    }

    @Override
    @Transactional
    public void deleteFieldById(UUID id) {
        companyTemplateFieldRepository.deleteById(id);
    }

    @Override
    @Transactional
    public List<CompanyTemplateField> createFields(CreateCompanyTemplateFieldCommand commands) {
        List<CompanyTemplateField> companyTemplateFields =
                        commands.templateFieldRequestDto()
                                .stream()
                                .map(field -> field.toEntity(commands.companyTemplate()))
                                .toList();

        return companyTemplateFieldRepository.saveAllAndFlush(companyTemplateFields);
    }
}
