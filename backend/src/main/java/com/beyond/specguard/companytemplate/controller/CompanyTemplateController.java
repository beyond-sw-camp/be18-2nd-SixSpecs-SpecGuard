package com.beyond.specguard.companytemplate.controller;

import com.beyond.specguard.companytemplate.model.dto.command.UpdateTemplateDetailCommand;
import com.beyond.specguard.companytemplate.model.dto.request.CompanyTemplateBasicRequestDto;
import com.beyond.specguard.companytemplate.model.dto.request.CompanyTemplateDetailRequestDto;
import com.beyond.specguard.companytemplate.model.dto.response.CompanyTemplateResponseDto;
import com.beyond.specguard.companytemplate.model.entity.CompanyTemplate;
import com.beyond.specguard.companytemplate.model.entity.CompanyTemplateField;
import com.beyond.specguard.companytemplate.model.service.CompanyTemplateFieldService;
import com.beyond.specguard.companytemplate.model.service.CompanyTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/company-template")
@RequiredArgsConstructor
@Tag(name = "CompanyTemplate", description = "회사 템플릿 관련 API")
public class CompanyTemplateController {

    private final CompanyTemplateService companyTemplateService;
    private final CompanyTemplateFieldService companyTemplateFieldService;

    @GetMapping("/{templateId}")
    public ResponseEntity<CompanyTemplateResponseDto> getCompanyTemplate(
            @PathVariable UUID templateId
    ) {
        CompanyTemplate companyTemplate = companyTemplateService.getCompanyTemplate(templateId);

        List<CompanyTemplateField> companyTemplateFields = companyTemplateFieldService.getFields(templateId);

        return new ResponseEntity<>(
                new CompanyTemplateResponseDto(companyTemplate, companyTemplateFields),
                HttpStatus.OK
        );
    }

    @Operation(
            summary = "기본 회사 템플릿 생성",
            description = "기본 정보만으로 회사 템플릿을 생성합니다.",
            responses = {
                @ApiResponse(
                        responseCode = "201",
                        description = "템플릿 생성 성공",
                        content = @Content(
                                mediaType = "application/json",
                                schema = @Schema(implementation = CompanyTemplateResponseDto.BasicDto.class)
                        )
                )
            }
    )
    @PostMapping("/basic")
    public ResponseEntity<CompanyTemplateResponseDto.BasicDto> createTemplate(
            @Parameter(description = "기본 템플릿 생성 요청 DTO", required = true)
            @Valid @RequestBody CompanyTemplateBasicRequestDto basicRequestDto
    ) {
        CompanyTemplate companyTemplate = basicRequestDto.toEntity();

        CompanyTemplate saved = companyTemplateService.createTemplate(companyTemplate);

        return new ResponseEntity<>(
                CompanyTemplateResponseDto.BasicDto.toDto(saved),
                HttpStatus.CREATED
        );
    }

    @Operation(
            summary = "상세 회사 템플릿 생성",
            description = "기존 템플릿에 상세 필드를 추가하여 회사 템플릿을 수정/확정 합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "템플릿 및 필드 생성 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = CompanyTemplateResponseDto.DetailDto.class)
                            )
                    ),
            }
    )
    @PostMapping("/detail")
    public ResponseEntity<CompanyTemplateResponseDto.DetailDto> createDetailTemplate(
        @Parameter(description = "상세 템플릿 생성 요청 DTO", required = true)
        @Validated(CompanyTemplateDetailRequestDto.Create.class)
        @RequestBody CompanyTemplateDetailRequestDto requestDto
    ) {
        CompanyTemplate companyTemplate = companyTemplateService.getCompanyTemplate(requestDto.getTemplateId());

        companyTemplate.update(requestDto);

        CompanyTemplate saved = companyTemplateService.createTemplate(companyTemplate);

        List< CompanyTemplateField> companyTemplateFields =
                companyTemplateFieldService.createFields(
                        requestDto.getFields()
                                .stream()
                                .map(field -> field.toEntity(companyTemplate))
                                .toList()
                );

        return new ResponseEntity<>(
                CompanyTemplateResponseDto.DetailDto.toDto(saved, companyTemplateFields),
                HttpStatus.CREATED
        );
    }

    @DeleteMapping("/{templateId}")
    public ResponseEntity<Message> deleteCompanyTemplate(
            @PathVariable UUID templateId
    ) {
        companyTemplateService.deleteTemplate(templateId);
        companyTemplateFieldService.deleteFields(templateId);
        return ResponseEntity.ok().body(
                new Message("기업 템플릿이 성공적으로 삭제되었습니다.")
        );
    }

    @PatchMapping("/{templateId}/basic")
    public ResponseEntity<CompanyTemplateResponseDto.BasicDto> patchBasicTemplate(
        @PathVariable UUID templateId,
        @Validated(CompanyTemplateBasicRequestDto.Update.class)
        @RequestBody CompanyTemplateBasicRequestDto requestDto
    ) {
        CompanyTemplate companyTemplate = companyTemplateService.getCompanyTemplate(templateId);

        companyTemplate.update(requestDto);

        CompanyTemplate updatedTemplate = companyTemplateService.updateTemplate(companyTemplate);

        return ResponseEntity.ok(CompanyTemplateResponseDto.BasicDto.toDto(updatedTemplate));

    }


    @PatchMapping("/{templateId}/detail")
    public ResponseEntity<CompanyTemplateResponseDto.DetailDto> patchDetailTemplate(
            @PathVariable UUID templateId,
            @Validated(CompanyTemplateDetailRequestDto.Update.class)
            @RequestBody CompanyTemplateDetailRequestDto requestDto
    ) {
        UpdateTemplateDetailCommand command = new UpdateTemplateDetailCommand(templateId, requestDto);
        CompanyTemplate companyTemplate = companyTemplateService.updateDetail(command);

        List<CompanyTemplateField> companyTemplateFields = companyTemplateFieldService.getFields(templateId);

        return ResponseEntity.ok(
                CompanyTemplateResponseDto.DetailDto.toDto(companyTemplate, companyTemplateFields)
        );
    }

    public record Message(String message) {}
}

