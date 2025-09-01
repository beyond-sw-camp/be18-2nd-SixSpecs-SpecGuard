package com.beyond.specguard.resume.dto.resume.response;

import com.beyond.specguard.resume.entity.common.enums.ResumeStatus;

public record ResumeListItem (
        String id,
        String name,
        String email,
        ResumeStatus status
){
}