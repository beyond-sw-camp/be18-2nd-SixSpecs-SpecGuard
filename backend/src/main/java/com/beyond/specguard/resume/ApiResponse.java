package com.beyond.specguard.resume;


import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 모든 API 응답을 통일된 형태로 감싸는 클래스
 * - status : 성공 여부 (true/false)
 * - message : 설명 메시지
 * - data : 실제 응답 데이터 (없을 수도 있음)
 */
@Getter
@AllArgsConstructor
public class ApiResponse<T> {

    private final boolean status;
    private final String message;
    private final T data;

    // 성공 응답
    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    // 실패 응답
    public static <T> ApiResponse<T> error(String message, T data) {
        return new ApiResponse<>(false, message, data);
    }
}

