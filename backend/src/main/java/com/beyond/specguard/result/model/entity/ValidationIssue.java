package com.beyond.specguard.result.model.entity;


import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(
        name="validation_issue"
)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
@ToString
public class ValidationIssue {
    @Id
    @GeneratedValue(strategy= GenerationType.UUID)
    @Column(length = 36, columnDefinition = "CHAR(36)")
    private UUID id;

    @Column(name="issue_type", nullable = false)
    private IssueType issueType;

    @Column(name="issue_description", columnDefinition = "TEXT", nullable = true)
    private String issueDescription;

    @Column(name="severity", nullable = false)
    private Severity severity;


    public enum IssueType{
        AUTH_API_FAILURE,                       //인증 API 연동 실패
        APPLICATION_FIELD_MISSING,              //지원서 필드 누락
        APPLICATION_VALUE_FORMAT_ERROR,         //지원서 값 형식 오류
        USER_IDENTIFICATION_FAILED,             //사용자 식별 불가
        COMPANY_MISMATCH,                       //회사 소석 불일치
        DUPLICATE_DATA_UPLOAD,                  //중복 데이터 업로드
        INVALID_ANALYSIS_VALUE,                 //비정상 데이터 분석값
        APPLICATION_VERSION_MISMATCH,           //지원서 버전 불일치
        MORPHOLOGICAL_ANALYSIS_FAILURE,         //형태소 분석 실패
        MODEL_PREDICTION_UNSTABLE               //모델 예측 불안정
    }

    //심각도
    public enum Severity{
        LOW,
        MEDIUM,
        HIGH
    }
}
