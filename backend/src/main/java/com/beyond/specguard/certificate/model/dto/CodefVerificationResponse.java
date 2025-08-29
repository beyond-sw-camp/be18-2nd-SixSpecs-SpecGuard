package com.beyond.specguard.certificate.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class CodefVerificationResponse {
    private Result result;
    private DataDto data;

    @Data
    public static class Result {
        private String code;
        private String extraMessage;
        private String message;
        private String transactionId;
    }

    @Data
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

    @Data
    public static class ResItem {
        private String resItemName;
        private String resPassDate;
    }
}