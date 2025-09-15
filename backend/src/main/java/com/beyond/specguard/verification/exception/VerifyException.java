package com.beyond.specguard.verification.exception;

import com.beyond.specguard.verification.exception.errorcode.VerifyErrorCode;
import lombok.Getter;
import org.springframework.security.core.AuthenticationException;

@Getter
public class VerifyException extends RuntimeException {
    private final VerifyErrorCode errorCode;


    public VerifyException(VerifyErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}

