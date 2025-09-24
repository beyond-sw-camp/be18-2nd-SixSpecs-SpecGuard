package com.beyond.specguard.crawling.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GitMetadataResponse {
    private Map<String, Double> languagePercentages;
    private LocalDateTime createdAt;

}
