package com.beyond.specguard.companytemplate.model.service;

import com.beyond.specguard.companytemplate.model.entity.CompanyTemplateField;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CompanyTemplateFieldServiceImpl implements CompanyTemplateFieldService {

    private final CompanyTemplateFieldRepository companyTemplateFieldRepository;

    @Override
    public List<CompanyTemplateField> createField(List<CompanyTemplateField> companyTemplateFields) {
        return companyTemplateFieldRepository.saveAll(companyTemplateFields);
    }
}
