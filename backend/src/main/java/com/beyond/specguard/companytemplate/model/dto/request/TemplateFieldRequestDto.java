package com.beyond.specguard.companytemplate.model.dto.request;

import com.beyond.specguard.companytemplate.model.entity.CompanyTemplate;
import com.beyond.specguard.companytemplate.model.entity.CompanyTemplateField;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class TemplateFieldRequestDto {

    @Schema(description = "필드 이름", example = "지원 동기")
    @NotBlank(message = "필드 이름은 필수 입력값입니다.")
    @Size(max = 100, message = "필드 이름은 최대 100자까지 입력 가능합니다.")
    private String fieldName;

    @Schema(description = "필드 타입 (예: TEXT, NUMBER, DATE, SELECT)", example = "TEXT")
    @NotNull(message = "필드 타입은 필수 입력값입니다.")
    private CompanyTemplateField.FieldType fieldType;

    @Schema(description = "필수 여부", example = "true")
    private boolean isRequired;

    @Schema(description = "출력 순서 (0부터 시작)", example = "1")
    @Min(value = 0, message = "출력 순서는 0 이상이어야 합니다.")
    private int fieldOrder;

    @Schema(description = "선택지 (JSON 배열 문자열 형태). 선택형 필드에서만 사용",
            example = "[\"예\", \"아니오\"]")
    private List<String> options;

    @Schema(description = "최소 길이", example = "10")
    @Min(value = 0, message = "최소 길이는 0 이상이어야 합니다.")
    private int minLength;

    @Schema(description = "최대 길이", example = "500")
    @Max(value = 500, message = "최대 길이는 500 이하여야 합니다.")
    private int maxLength;

    public CompanyTemplateField toEntity(CompanyTemplate companyTemplate) {
        try{
            return CompanyTemplateField.builder()
                    .template(companyTemplate)
                    .fieldName(fieldName)
                    .fieldType(fieldType)
                    .isRequired(isRequired)
                    .fieldOrder(fieldOrder)
                    .minLength(minLength)
                    .maxLength(maxLength)
                    .options(new ObjectMapper().writeValueAsString(options))
                    .build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
