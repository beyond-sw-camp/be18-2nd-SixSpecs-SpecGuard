package com.beyond.specguard.companytemplate.exception.ErrorCode;

import com.beyond.specguard.common.exception.errorcode.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum CompanyTemplateErrorCode implements ErrorCode {
    TEMPLATE_NOT_FOUND(HttpStatus.NOT_FOUND, "TEMPLATE_NOT_FOUND", "Company Template Not Found"),;

    private final HttpStatus status;
    private final String code;
    private final String message;

    CompanyTemplateErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
