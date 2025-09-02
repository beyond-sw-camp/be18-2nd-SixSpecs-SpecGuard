package com.beyond.specguard.resume.dto.link;

import com.beyond.specguard.resume.entity.common.enums.LinkType;

public record ResumeLinkCreateRequest(
        String url,
        LinkType linkType,
        String contents
) {
}
