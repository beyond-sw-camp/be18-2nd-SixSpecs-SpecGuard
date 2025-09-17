package com.beyond.specguard.githubcrawling.exception;

import com.beyond.specguard.common.exception.CustomException;
import com.beyond.specguard.githubcrawling.exception.errorcode.GitErrorCode;

public class GitException extends CustomException {
    public GitException(GitErrorCode errorCode) {
        super(errorCode);
    }
}
