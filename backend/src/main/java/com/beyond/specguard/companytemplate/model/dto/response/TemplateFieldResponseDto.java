package com.beyond.specguard.companytemplate.model.dto.response;

import com.beyond.specguard.companytemplate.model.entity.CompanyTemplateField;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TemplateFieldResponseDto {
    private UUID id;
    private String fieldName;
    private CompanyTemplateField.FieldType fieldType;
    private boolean isRequired;
    private Integer fieldOrder;
    private String options;
    private Integer minLength;
    private Integer maxLength;

    public static TemplateFieldResponseDto fromEntity(CompanyTemplateField field) {
        return TemplateFieldResponseDto.builder()
                .id(field.getId())
                .fieldName(field.getFieldName())
                .fieldType(field.getFieldType())
                .isRequired(field.isRequired())
                .fieldOrder(field.getFieldOrder())
                .options(field.getOptions())
                .minLength(field.getMinLength())
                .maxLength(field.getMaxLength())
                .build();
    }
}
