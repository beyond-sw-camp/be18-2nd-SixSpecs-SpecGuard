package com.beyond.specguard.plan.exception.errorcode;

import com.beyond.specguard.common.exception.errorcode.CommonErrorCode;
import com.beyond.specguard.common.exception.errorcode.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum PlanErrorCode implements ErrorCode {

    // 생성 관련
    DUPLICATE_PLAN_NAME(HttpStatus.CONFLICT,"DUPLICATE_PLAN_NAME", "이미 존재하는 요금제 이름입니다."),

    // 조회 관련
    PLAN_NOT_FOUND(HttpStatus.NOT_FOUND,"PLAN_NOT_FOUND", "해당 요금제를 찾을 수 없습니다."),

    // 업데이트 관련
    INVALID_PLAN_UPDATE(HttpStatus.BAD_REQUEST,"INVALID_PLAN_UPDATE", "해당 요금제는 수정할 수 없습니다."),

    // 삭제 관련
    PLAN_IN_USE(HttpStatus.BAD_REQUEST,"PLAN_IN_USE", "현재 사용 중인 요금제는 삭제할 수 없습니다."),

    // 범용
    INVALID_PLAN_REQUEST(HttpStatus.BAD_REQUEST,"INVALID_PLAN_REQUEST", "잘못된 요금제 요청입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    PlanErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
