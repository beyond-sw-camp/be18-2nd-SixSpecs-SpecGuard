package com.beyond.specguard.certificate.util;

import com.beyond.specguard.common.exception.CustomException;
import com.beyond.specguard.resume.exception.errorcode.ResumeErrorCode;

public class CertificateNumberUtil {
    /**
     * 자격증 번호 전처리
     * - "HRD" 접두사 제거
     * - 모든 하이픈("-") 제거
     * - 앞뒤 공백 제거
     *
     * @param rawCertificateNo 원본 자격증 번호
     * @return 전처리된 자격증 번호 (숫자만)
     */
    public static String preprocessCertificateNumber(String rawCertificateNo) {
        if (rawCertificateNo == null || rawCertificateNo.isBlank()) {
            throw new CustomException(ResumeErrorCode.INVALID_CERTIFICATE);
        }

        // 공백 제거
        String result = rawCertificateNo.trim();

        // HRD 접두사 제거 (대소문자 무시)
        if (result.toUpperCase().startsWith("HRD")) {
            result = result.substring(3);
        }

        // 모든 하이픈 제거
        result = result.replaceAll("-", "");

        // 최종 검증: 숫자만 남는지 체크
        if (!result.matches("\\d+")) {
            throw new CustomException(ResumeErrorCode.INVALID_CERTIFICATE);
        }

        return result;
    }
}
