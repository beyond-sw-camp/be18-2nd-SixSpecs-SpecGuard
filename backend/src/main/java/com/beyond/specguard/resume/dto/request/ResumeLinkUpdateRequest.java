package com.beyond.specguard.resume.dto.request;

import com.beyond.specguard.resume.entity.common.enums.LinkType;

public record ResumeLinkUpdateRequest(
        String url,
        LinkType linkType,
        String contents
) {
}
