package com.beyond.specguard.companytemplate.controller;

import com.beyond.specguard.company.common.model.entity.ClientUser;
import com.beyond.specguard.company.common.model.service.CustomUserDetails;
import com.beyond.specguard.common.validation.Create;
import com.beyond.specguard.common.validation.Update;
import com.beyond.specguard.companytemplate.model.dto.command.CreateBasicCompanyTemplateCommand;
import com.beyond.specguard.companytemplate.model.dto.command.CreateDetailCompanyTemplateCommand;
import com.beyond.specguard.companytemplate.model.dto.command.SearchTemplateCommand;
import com.beyond.specguard.companytemplate.model.dto.command.UpdateTemplateBasicCommand;
import com.beyond.specguard.companytemplate.model.dto.command.UpdateTemplateDetailCommand;
import com.beyond.specguard.companytemplate.model.dto.request.CompanyTemplateBasicRequestDto;
import com.beyond.specguard.companytemplate.model.dto.request.CompanyTemplateDetailRequestDto;
import com.beyond.specguard.companytemplate.model.dto.response.CompanyTemplateListResponseDto;
import com.beyond.specguard.companytemplate.model.dto.response.CompanyTemplateResponseDto;
import com.beyond.specguard.companytemplate.model.entity.CompanyTemplate;
import com.beyond.specguard.companytemplate.model.service.CompanyTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/companyTemplates")
@RequiredArgsConstructor
@Tag(name = "CompanyTemplate", description = "회사 템플릿 관련 API")
public class CompanyTemplateController {

    private final CompanyTemplateService companyTemplateService;

    @Operation(
            summary = "단일 회사 템플릿 조회",
            description = "템플릿 ID를 기반으로 상세 정보를 조회합니다."
    )
    @GetMapping("/{templateId}")
    public ResponseEntity<CompanyTemplateResponseDto> getCompanyTemplate(
            @PathVariable UUID templateId,
            Authentication authentication
    ) {
        ClientUser clientUser = getClientUser(authentication);
        CompanyTemplate companyTemplate = companyTemplateService.getCompanyTemplate(clientUser, templateId);

        return new ResponseEntity<>(
                new CompanyTemplateResponseDto(companyTemplate),
                HttpStatus.OK
        );
    }

    @Operation(
            summary = "회사 템플릿 목록 조회",
            description = "부서, 직무, 기간, 상태, 연차 조건으로 회사 템플릿 목록을 조회합니다."
    )
    @GetMapping
    public ResponseEntity<CompanyTemplateListResponseDto> getTemplates(
            @Parameter(description = "부서 필터") @RequestParam(required = false) String department,
            @Parameter(description = "직무 필터") @RequestParam(required = false) String category,
            @Parameter(description = "시작일 필터") @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "마감일 필터") @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "템플릿 상태 필터") @RequestParam(required = false) CompanyTemplate.TemplateStatus status,
            @Parameter(description = "연차 필터") @RequestParam(required = false) Integer yearsOfExperience,
            @ParameterObject @Parameter(description = "페이지 정보") @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            Authentication authentication
    ) {
        ClientUser clientUser =  getClientUser(authentication);

        SearchTemplateCommand templateCommand = SearchTemplateCommand.builder()
                .clientUser(clientUser)
                .department(department)
                .category(category)
                .startDate(startDate)
                .endDate(endDate)
                .status(status)
                .yearsOfExperience(yearsOfExperience)
                .pageable(pageable)
                .build();

        CompanyTemplateListResponseDto response = companyTemplateService.getTemplates(templateCommand);
        return ResponseEntity.ok(response);
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
                ),
                @ApiResponse(responseCode = "400", description = "잘못된 요청")
            }
    )
    @PostMapping("/basic")
    public ResponseEntity<CompanyTemplateResponseDto.BasicDto> createBasicTemplate(
            @Parameter(description = "기본 템플릿 생성 요청 DTO", required = true)
            @Validated(Create.class) @RequestBody
            CompanyTemplateBasicRequestDto basicRequestDto,
            Authentication authentication
    ) {
        ClientUser clientUser = getClientUser(authentication);
        CompanyTemplateResponseDto.BasicDto saved = companyTemplateService.createBasicTemplate(
                new CreateBasicCompanyTemplateCommand(clientUser, basicRequestDto)
        );

        return new ResponseEntity<>(
                saved,
                HttpStatus.CREATED
        );
    }

    private ClientUser getClientUser(Authentication authentication) {
        return ((CustomUserDetails) authentication.getPrincipal()).getUser();
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
                    @ApiResponse(responseCode = "400", description = "잘못된 요청")
            }
    )
    @PostMapping("/detail")
    public ResponseEntity<CompanyTemplateResponseDto.DetailDto> createDetailTemplate(
        @Parameter(description = "상세 템플릿 생성 요청 DTO", required = true)
        @Validated(Create.class)
        @RequestBody CompanyTemplateDetailRequestDto requestDto,
        Authentication authentication
    ) {
        ClientUser clientUser = getClientUser(authentication);

        // Template, Field 생성
        CompanyTemplateResponseDto.DetailDto companyTemplate = companyTemplateService.createDetailTemplate(
                new CreateDetailCompanyTemplateCommand(clientUser, requestDto)
        );

        return new ResponseEntity<>(companyTemplate, HttpStatus.CREATED);
    }

    @Operation(
            summary = "회사 템플릿 삭제",
            description = "템플릿 ID를 기반으로 회사 템플릿을 삭제합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "삭제 성공"),
                    @ApiResponse(responseCode = "404", description = "템플릿을 찾을 수 없음")
            }
    )
    @DeleteMapping("/{templateId}")
    public ResponseEntity<Void> deleteCompanyTemplate(
            @PathVariable UUID templateId,
            Authentication authentication
    ) {
        ClientUser clientUser = getClientUser(authentication);

        companyTemplateService.deleteTemplate(templateId, clientUser);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "기본 템플릿 정보 수정",
            description = "기본 템플릿 정보를 부분 수정합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "수정 성공",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = CompanyTemplateResponseDto.BasicDto.class))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "404", description = "템플릿을 찾을 수 없음")
            }
    )
    @PatchMapping("/{templateId}/basic")
    public ResponseEntity<CompanyTemplateResponseDto.BasicDto> patchBasicTemplate(
        @PathVariable UUID templateId,
        @Validated(Update.class)
        @RequestBody CompanyTemplateBasicRequestDto requestDto,
        Authentication authentication
    ) {
        ClientUser clientUser = getClientUser(authentication);

        UpdateTemplateBasicCommand updateTemplateBasicCommand = new UpdateTemplateBasicCommand(templateId, requestDto, clientUser);

        CompanyTemplateResponseDto.BasicDto updatedTemplate = companyTemplateService.updateBasic(updateTemplateBasicCommand);

        return ResponseEntity.ok(updatedTemplate);
    }


    @Operation(
            summary = "상세 템플릿 정보 수정",
            description = "상세 템플릿 정보를 부분 수정합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "수정 성공",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = CompanyTemplateResponseDto.DetailDto.class))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "404", description = "템플릿을 찾을 수 없음")
            }
    )
    @PatchMapping("/{templateId}/detail")
    public ResponseEntity<CompanyTemplateResponseDto.DetailDto> patchDetailTemplate(
            @PathVariable UUID templateId,
            @Validated(Update.class)
            @RequestBody CompanyTemplateDetailRequestDto requestDto,
            Authentication authentication
    ) {
        ClientUser clientUser = getClientUser(authentication);

        UpdateTemplateDetailCommand command = new UpdateTemplateDetailCommand(templateId, requestDto, clientUser);
        CompanyTemplateResponseDto.DetailDto responseDto = companyTemplateService.updateDetail(command);

        return ResponseEntity.ok(responseDto);
    }
}

