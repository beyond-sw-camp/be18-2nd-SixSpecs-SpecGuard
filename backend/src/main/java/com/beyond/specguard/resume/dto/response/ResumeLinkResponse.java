package com.beyond.specguard.resume.dto.response;

import com.beyond.specguard.resume.entity.common.enums.LinkType;

import java.util.UUID;

public record ResumeLinkResponse(
        UUID id,
        String url,
        String linkType,
        String label
) {
}
