package com.beyond.specguard.companytemplate.model.dto.response;

import com.beyond.specguard.companytemplate.model.entity.CompanyTemplateField;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TemplateFieldResponseDto {
    private String fieldName;
    private CompanyTemplateField.FieldType fieldType;
    private boolean isRequired;
    private Integer fieldOrder;
    private String options;
    private Integer minLength;
    private Integer maxLength;

    public TemplateFieldResponseDto (CompanyTemplateField template) {
        this.fieldName = template.getFieldName();
        this.fieldType = template.getFieldType();
        this.isRequired = template.isRequired();
        this.fieldOrder = template.getFieldOrder();
        this.options = template.getOptions();
        this.minLength = template.getMinLength();
        this.maxLength = template.getMaxLength();
    }
}
