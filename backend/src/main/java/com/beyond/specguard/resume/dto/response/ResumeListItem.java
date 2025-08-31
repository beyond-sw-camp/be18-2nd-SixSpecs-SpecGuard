package com.beyond.specguard.resume.dto.response;

import com.beyond.specguard.resume.entity.common.enums.ResumeStatus;

public record ResumeListItem (
        String id,
        String name,
        String email,
        ResumeStatus status,
        String applyField,
        String profileImageUrl
){
}