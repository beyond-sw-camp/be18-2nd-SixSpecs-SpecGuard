package com.beyond.specguard.invite.exception;

import com.beyond.specguard.common.exception.CustomException;
import com.beyond.specguard.invite.exception.errorcode.InviteErrorCode;

public class InviteException extends CustomException {
    public InviteException(InviteErrorCode errorCode) {
        super(errorCode);
    }
}
