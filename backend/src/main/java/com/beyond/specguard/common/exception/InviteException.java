package com.beyond.specguard.common.exception;

import com.beyond.specguard.common.exception.errorcode.InviteErrorCode;

public class InviteException extends CustomException {
    public InviteException(InviteErrorCode errorCode) {
        super(errorCode);
    }
}
