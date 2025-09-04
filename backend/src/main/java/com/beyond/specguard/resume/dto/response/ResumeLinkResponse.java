package com.beyond.specguard.resume.dto.response;

import com.beyond.specguard.resume.entity.common.enums.LinkType;

public record ResumeLinkResponse(
        String id,
        String resumeId,
        String url,
        LinkType linkType,
        String contents,
        String createdAt,
        String updatedAt
) {
}
