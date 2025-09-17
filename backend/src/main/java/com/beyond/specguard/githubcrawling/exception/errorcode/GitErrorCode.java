package com.beyond.specguard.githubcrawling.exception.errorcode;

import com.beyond.specguard.common.exception.errorcode.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum GitErrorCode implements ErrorCode {
    GITHUB_API_ERROR(HttpStatus.BAD_GATEWAY, "GITHUB_API_ERROR", "GitHub API 호출 실패"),
    GITHUB_INVALID_URL(HttpStatus.BAD_REQUEST, "GITHUB_INVALID_URL", "잘못된 GitHub URL 입니다."),
    GITHUB_PARSE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "GITHUB_PARSE_ERROR", "GitHub 응답 파싱 실패"),
    GITHUB_UNKNOWN(HttpStatus.INTERNAL_SERVER_ERROR, "GITHUB_UNKNOWN", "알 수 없는 GitHub 크롤링 오류");

    private final HttpStatus status;
    private final String code;
    private final String message;

    GitErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
