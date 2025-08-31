package com.beyond.specguard.resume.util;

import org.springframework.stereotype.Component;

@Component
public class TemplateResolver {

    private String defaultTemplateId;

    public String resolveDefaultTemplateIdForSignUp() {
        return defaultTemplateId;                                  // 생성 시 사용할 기본 템플릿 ID 반환
    }
}
