package com.beyond.specguard.certificate.model.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CodefVerificationResponse {
    private Result result;
    private DataDto data;

    @Getter
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class Result {
        private String code;
        private String extraMessage;
        private String message;
        private String transactionId;
    }

    @Getter
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class DataDto {
        private String resIssueYN;
        private String resResultDesc;
        private String resDocNo;
        private String resPublishNo;
        private String resType;
        private String resUserNm;
        private String resExaminationNo;
        private String resAcquisitionDate;
        private String resInquiryDate;
        private String commBirthDate;
        private String resDocType;
        private List<ResItem> resItemList;
    }

    @Getter
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class ResItem {
        private String resItemName;
        private String resPassDate;
    }
}