package com.beyond.specguard.resume.model.dto.response;

import com.beyond.specguard.crawling.dto.GitMetadataResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResumeGitSummaryResponse {
    private ResumeResponse resume;
    private GitMetadataResponse gitMetadata;
    private List<String> summaries;   // 요약 여러 개

}
