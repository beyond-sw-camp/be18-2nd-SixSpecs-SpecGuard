package com.beyond.specguard.companytemplate.model.service;

import com.beyond.specguard.common.exception.CustomException;
import com.beyond.specguard.companytemplate.exception.ErrorCode.CompanyTemplateErrorCode;
import com.beyond.specguard.companytemplate.model.dto.command.CreateCompanyTemplateFieldCommand;
import com.beyond.specguard.companytemplate.model.dto.command.UpdateTemplateBasicCommand;
import com.beyond.specguard.companytemplate.model.dto.command.UpdateTemplateDetailCommand;
import com.beyond.specguard.companytemplate.model.dto.request.CompanyTemplateBasicRequestDto;
import com.beyond.specguard.companytemplate.model.dto.request.CompanyTemplateDetailRequestDto;
import com.beyond.specguard.companytemplate.model.dto.request.TemplateFieldRequestDto;
import com.beyond.specguard.companytemplate.model.entity.CompanyTemplate;
import com.beyond.specguard.companytemplate.model.entity.CompanyTemplateField;
import com.beyond.specguard.companytemplate.model.repository.CompanyTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
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
    // 부모 Transaction에 참여하지 않음
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public CompanyTemplate getCompanyTemplate(UUID templateId) {
        return companyTemplateRepository.findById(templateId)
                .orElseThrow(() -> new CustomException(CompanyTemplateErrorCode.TEMPLATE_NOT_FOUND));
    }

    @Override
    @Transactional
    public void deleteTemplate(UUID templateId) {
        // id가 존재하지 않으면 예외 던지기
        if (!companyTemplateRepository.existsById(templateId)) {
            throw new CustomException(CompanyTemplateErrorCode.TEMPLATE_NOT_FOUND);
        }

        // template 삭제
        companyTemplateRepository.deleteById(templateId);

        // fk 제약이 없으므로  templateField 수동 삭제
        companyTemplateFieldService.deleteFields(templateId);
    }

    @Override
    @Transactional
    public CompanyTemplate updateBasic(UpdateTemplateBasicCommand command) {
        CompanyTemplate companyTemplate = getCompanyTemplate(command.templateId());

        companyTemplate.update(command.requestDto());

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
        Map<UUID, TemplateFieldRequestDto> dtoMap = command.requestDto().fields().stream()
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
        command.requestDto().fields().stream()
                .filter(f -> f.getId() == null)
                .forEach(newDto -> {
                    CreateCompanyTemplateFieldCommand companyTemplateFieldCommand = new CreateCompanyTemplateFieldCommand(updatedTemplate, newDto);
                    companyTemplateFieldService.createField(companyTemplateFieldCommand);
                });

        return updatedTemplate;
    }

    @Override
    @Transactional
    public CompanyTemplate createDetailTemplate(CompanyTemplateDetailRequestDto requestDto) {
        UUID templateId = requestDto.detailDto().getTemplateId();

        // 1. CompanyTemplate Detail 부분 반영해서 업데이트
        CompanyTemplate companyTemplate = getCompanyTemplate(templateId);

        // 1.1 사용 완료 상태 True로 하기
        companyTemplate.setStatusActive();

        CompanyTemplate createdTemplate = companyTemplateRepository.save(companyTemplate);

        // 2. Field 저장
        // 2.1 CreateCompanyTemplateFieldCommand로 변경
        List<CreateCompanyTemplateFieldCommand> commands = requestDto.fields()
                .stream()
                .map(dto -> new CreateCompanyTemplateFieldCommand(createdTemplate, dto))
                .toList();

        // 2.2 Field 저장
        companyTemplateFieldService.createFields(commands);

        return createdTemplate;
    }

    @Override
    public CompanyTemplate createBasicTemplate(CompanyTemplateBasicRequestDto basicRequestDto) {
        // BasicRequestDto 에서 받은 정보를 토대로 entity 생성
        // 그런데, not null 제약조건 있는 것은 기본 값 삽입하기.
        CompanyTemplate companyTemplate = basicRequestDto.toEntity();

        return companyTemplateRepository.save(companyTemplate);
    }
}
