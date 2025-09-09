package com.beyond.specguard.companytemplate.exception.ErrorCode;

import com.beyond.specguard.common.exception.errorcode.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum CompanyTemplateErrorCode implements ErrorCode {
    TEMPLATE_NOT_FOUND(HttpStatus.NOT_FOUND, "TEMPLATE_NOT_FOUND", "해당 템플릿을 찾을 수 없습니다."),
    TEMPLATE_FIELD_NOT_FOUND(HttpStatus.NOT_FOUND, "TEMPLATE_FIELD_NOT_FOUND", "해당 템플릿 항목을 찾을 수 없습니다."),
    NOT_DRAFT_TEMPLATE(HttpStatus.CONFLICT, "NOT_DRAFT_TEMPLATE", "DRAFT 상태의 템플릿만 수정할 수 있습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    CompanyTemplateErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
