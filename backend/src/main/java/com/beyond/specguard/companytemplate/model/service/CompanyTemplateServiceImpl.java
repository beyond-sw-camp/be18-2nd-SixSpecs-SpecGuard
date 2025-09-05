package com.beyond.specguard.companytemplate.model.service;

import com.beyond.specguard.common.exception.CustomException;
import com.beyond.specguard.companytemplate.exception.ErrorCode.CompanyTemplateErrorCode;
import com.beyond.specguard.companytemplate.model.dto.command.CreateCompanyTemplateFieldCommand;
import com.beyond.specguard.companytemplate.model.dto.command.UpdateTemplateDetailCommand;
import com.beyond.specguard.companytemplate.model.dto.request.TemplateFieldRequestDto;
import com.beyond.specguard.companytemplate.model.entity.CompanyTemplate;
import com.beyond.specguard.companytemplate.model.entity.CompanyTemplateField;
import com.beyond.specguard.companytemplate.model.repository.CompanyTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompanyTemplateServiceImpl implements CompanyTemplateService {
    private final CompanyTemplateRepository companyTemplateRepository;
    private final CompanyTemplateFieldService companyTemplateFieldService;

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
    public CompanyTemplate updateDetail(UpdateTemplateDetailCommand command) {
        UUID templateId = command.templateId();

        // 0. template 조회 후 detail 업데이트
        CompanyTemplate template = getCompanyTemplate(templateId);

        template.update(command.requestDto());
        final CompanyTemplate updatedTemplate = companyTemplateRepository.save(template);

        // 1. 기존 필드 로드
        List<CompanyTemplateField> existingFields = companyTemplateFieldService.getFields(templateId);

        // 2. 요청 필드 id → dto 매핑
        Map<UUID, TemplateFieldRequestDto> dtoMap = command.requestDto().getFields().stream()
                .filter(f -> f.getId() != null)
                .collect(Collectors.toMap(TemplateFieldRequestDto::getId, f -> f));

        // 3. 업데이트 & 삭제
        for (CompanyTemplateField existing : existingFields) {
            if (dtoMap.containsKey(existing.getId())) {
                // 업데이트
                existing.update(dtoMap.get(existing.getId()));
            } else {
                // 요청에 없음 → 삭제
                companyTemplateFieldService.deleteFieldById(existing.getId());
            }
        }

        // 4. 신규 생성
        command.requestDto().getFields().stream()
                .filter(f -> f.getId() == null)
                .forEach(newDto -> {
                    CreateCompanyTemplateFieldCommand companyTemplateFieldCommand = new CreateCompanyTemplateFieldCommand(updatedTemplate, newDto);
                    companyTemplateFieldService.createField(companyTemplateFieldCommand);
                });

        return updatedTemplate;
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
