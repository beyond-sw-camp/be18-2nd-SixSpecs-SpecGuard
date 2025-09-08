package com.beyond.specguard.companytemplate.model.service;

import com.beyond.specguard.auth.model.entity.ClientUser;
import com.beyond.specguard.common.exception.CustomException;
import com.beyond.specguard.common.exception.errorcode.CommonErrorCode;
import com.beyond.specguard.companytemplate.exception.ErrorCode.CompanyTemplateErrorCode;
import com.beyond.specguard.companytemplate.model.dto.command.CreateBasicCompanyTemplateCommand;
import com.beyond.specguard.companytemplate.model.dto.command.CreateCompanyTemplateFieldCommand;
import com.beyond.specguard.companytemplate.model.dto.command.CreateDetailCompanyTemplateCommand;
import com.beyond.specguard.companytemplate.model.dto.command.SearchTemplateCommand;
import com.beyond.specguard.companytemplate.model.dto.command.UpdateTemplateBasicCommand;
import com.beyond.specguard.companytemplate.model.dto.command.UpdateTemplateDetailCommand;
import com.beyond.specguard.companytemplate.model.dto.request.TemplateFieldRequestDto;
import com.beyond.specguard.companytemplate.model.dto.response.CompanyTemplateResponseDto;
import com.beyond.specguard.companytemplate.model.entity.CompanyTemplate;
import com.beyond.specguard.companytemplate.model.entity.CompanyTemplateField;
import com.beyond.specguard.companytemplate.model.repository.CompanyTemplateRepository;
import com.beyond.specguard.companytemplate.model.spec.CompanyTemplateSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.EnumSet;
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
    @Transactional(readOnly = true)
    public CompanyTemplate getCompanyTemplate(UUID templateId) {
        return companyTemplateRepository.findById(templateId)
                .orElseThrow(() -> new CustomException(CompanyTemplateErrorCode.TEMPLATE_NOT_FOUND));
    }

    @Override
    @Transactional
    public void deleteTemplate(UUID templateId, ClientUser clientUser) {
        // 권한 검증
        validateWriteRole(clientUser.getRole());

        // templateId가 존재하지 않으면 예외 던지기
        if (!companyTemplateRepository.existsById(templateId)) {
            throw new CustomException(CompanyTemplateErrorCode.TEMPLATE_NOT_FOUND);
        }

        // template 삭제
        companyTemplateRepository.deleteById(templateId);

        // 템플릿에 관련된 templateField 도 삭제
        companyTemplateFieldService.deleteFields(templateId);
    }

    private void validateWriteRole(ClientUser.Role role) {
        if (!EnumSet.of(ClientUser.Role.OWNER, ClientUser.Role.MANAGER).contains(role)) {
            throw new CustomException(CommonErrorCode.ACCESS_DENIED);
        }
    }

    @Override
    @Transactional
    public CompanyTemplateResponseDto.BasicDto updateBasic(UpdateTemplateBasicCommand command) {
        // Write 권한 검증
        validateWriteRole(command.clientUser().getRole());

        CompanyTemplate companyTemplate = getCompanyTemplate(command.templateId());

        companyTemplate.update(command.requestDto());

        return CompanyTemplateResponseDto.BasicDto.toDto(companyTemplateRepository.save(companyTemplate));
    }

    @Override
    @Transactional
    public CompanyTemplateResponseDto.DetailDto updateDetail(UpdateTemplateDetailCommand command) {
        // 1. 쓰기 권한 검증
        validateWriteRole(command.clientUser().getRole());

        // 2. template 조회 후 detail 업데이트
        CompanyTemplate template = companyTemplateRepository.findById(command.templateId())
                .orElseThrow(()-> new CustomException(CompanyTemplateErrorCode.TEMPLATE_NOT_FOUND));

        // 3. 조회된 template과 유저의 company 값이 같은지 확인
        validateCompany(command.clientUser().getCompany().getId(), template.getClientCompany().getId());

        // 4. 조회된 template 내용 업데이트
        template.update(command.requestDto());

        List<CompanyTemplateField> existingFields = companyTemplateFieldService.getFields(command.templateId());
        List<CompanyTemplateField> updatedFields = new ArrayList<>();

        // 5. 요청 필드 id → dto 매핑
        Map<UUID, TemplateFieldRequestDto> dtoMap = command.requestDto().fields().stream()
                .filter(f -> f.getId() != null)
                .collect(Collectors.toMap(TemplateFieldRequestDto::getId, f -> f));

        // 6. 업데이트 & 삭제
        for (CompanyTemplateField existing : existingFields) {
            if (dtoMap.containsKey(existing.getId())) {
                // 업데이트
                existing.update(dtoMap.get(existing.getId()));
                updatedFields.add(existing);
            } else {
                // 요청에 없음 → 삭제
                companyTemplateFieldService.deleteFieldById(existing.getId());
            }
        }


        CreateCompanyTemplateFieldCommand companyTemplateFieldCommand = new CreateCompanyTemplateFieldCommand(
                template,
                command.requestDto().fields().stream()
                        .filter(f -> f.getId() == null)
                        .toList());

        // 7. 신규 생성
        updatedFields.addAll(companyTemplateFieldService.createFields(companyTemplateFieldCommand));

        // 8. 필드 업데이트
        updatedFields.forEach(template::addField);

        // 9. 세이브
        return CompanyTemplateResponseDto.DetailDto.toDto(companyTemplateRepository.save(template));
    }

    private void validateCompany(UUID company, UUID id) {
        if (!company.equals(id))
            throw new CustomException(CommonErrorCode.ACCESS_DENIED);
    }

    @Override
    @Transactional
    public CompanyTemplateResponseDto.DetailDto createDetailTemplate(CreateDetailCompanyTemplateCommand command) {

        // 권한 검증
        validateWriteRole(command.clientUser().getRole());

        UUID templateId = command.requestDto().detailDto().getTemplateId();

        // 1. CompanyTemplate Detail 부분 반영해서 업데이트
        CompanyTemplate template = companyTemplateRepository.findById(templateId)
                .orElseThrow(() -> new CustomException(CompanyTemplateErrorCode.TEMPLATE_NOT_FOUND));

        // 2. 생성 완료 상태로 하기
        template.setStatusActive();

        // 3. Field 저장
        List<CompanyTemplateField> fields = companyTemplateFieldService.createFields(
                new CreateCompanyTemplateFieldCommand(template, command.requestDto().fields())
        );

        fields.forEach(template::addField);

        // 4. CompanyTemplate 저장
        CompanyTemplate companyTemplate  = companyTemplateRepository.save(template);

        return CompanyTemplateResponseDto.DetailDto.toDto(companyTemplate);
    }

    @Override
    @Transactional
    public CompanyTemplateResponseDto.BasicDto createBasicTemplate(CreateBasicCompanyTemplateCommand command) {
        // 역할 검증
        validateWriteRole(command.clientUser().getRole());

        // BasicRequestDto 에서 받은 정보를 토대로 entity 생성
        // not null 제약조건 필드에는 기본 값 삽입하기.
        CompanyTemplate companyTemplate = command.basicRequestDto().toEntity(command.clientUser().getCompany());

        return CompanyTemplateResponseDto.BasicDto.toDto(companyTemplate);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CompanyTemplate> getTemplates(SearchTemplateCommand c) {
        Specification<CompanyTemplate> spec = Specification.allOf(
                CompanyTemplateSpecification.hasDepartment(c.department()),
                CompanyTemplateSpecification.hasCategory(c.category()),
                CompanyTemplateSpecification.startDateAfter(c.startDate().atStartOfDay()),
                CompanyTemplateSpecification.endDateBefore(c.endDate().atTime(LocalTime.MAX)),
                CompanyTemplateSpecification.hasStatus(c.status()),
                CompanyTemplateSpecification.hasYearsOfExperience(c.yearsOfExperience())
        );

        return companyTemplateRepository.findAll(spec, c.pageable());
    }

}
