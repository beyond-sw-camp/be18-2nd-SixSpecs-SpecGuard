package com.beyond.specguard.resume.model.dto.response;

import com.beyond.specguard.crawling.dto.GitMetadataResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResumeWithGitResponse {
    private ResumeResponse resume;
    private GitMetadataResponse gitMetadata;
}
