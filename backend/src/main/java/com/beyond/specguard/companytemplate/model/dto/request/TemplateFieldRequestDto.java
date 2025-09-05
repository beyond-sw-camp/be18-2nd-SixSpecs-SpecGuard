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
import java.util.UUID;

@Getter
@NoArgsConstructor
public class TemplateFieldRequestDto {

    @NotNull(groups = Update.class, message = "업데이트 시 id 값은 필수 입력값입니다.")
    private UUID id;

    @Schema(description = "필드 이름", example = "지원 동기")
    @NotBlank(groups = Create.class, message = "필드 이름은 필수 입력값입니다.")
    @Size(max = 100, message = "필드 이름은 최대 100자까지 입력 가능합니다.")
    private String fieldName;

    @Schema(description = "필드 타입 (TEXT, NUMBER, DATE, SELECT)", example = "TEXT")
    @NotNull(groups = Create.class, message = "필드 타입은 필수 입력값입니다.")
    private CompanyTemplateField.FieldType fieldType;

    @Schema(description = "필수 여부", example = "true")
    @NotNull(groups = Create.class, message = "응답 필수 여부는 필수 입력값입니다.")
    private Boolean isRequired;

    @Schema(description = "출력 순서 (0부터 시작)", example = "1")
    @Min(value = 0, message = "출력 순서는 0 이상이어야 합니다.")
    private Integer fieldOrder;

    @Schema(description = "선택지 (JSON 배열 문자열 형태). 선택형 필드에서만 사용",
            example = "[\"예\", \"아니오\"]")
    private List<String> options;

    @Schema(description = "최소 길이", example = "10")
    @Min(value = 0, message = "최소 길이는 0 이상이어야 합니다.")
    private Integer minLength;

    @Schema(description = "최대 길이", example = "500")
    @Max(value = 2000, message = "최대 길이는 2000 이하여야 합니다.")
    private Integer maxLength;

    public String getOptionsByString() {
        try {
            return new ObjectMapper().writeValueAsString(this.options);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public CompanyTemplateField toEntity(CompanyTemplate companyTemplate) {
        return CompanyTemplateField.builder()
                .template(companyTemplate)
                .fieldName(fieldName)
                .fieldType(fieldType)
                .isRequired(isRequired)
                .fieldOrder(fieldOrder)
                .minLength(minLength)
                .maxLength(maxLength)
                .options(getOptionsByString())
                .build();

    }

    public interface Create {}
    public interface Update {}
}
