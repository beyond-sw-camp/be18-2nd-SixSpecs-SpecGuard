package com.beyond.specguard.certificate.model.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class CodefVerificationResponse {
    private Map<String, Object> result;
    private List<Map<String, Object>> lists;
}