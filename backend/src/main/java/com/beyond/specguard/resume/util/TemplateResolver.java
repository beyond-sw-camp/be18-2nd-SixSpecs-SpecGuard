package com.beyond.specguard.resume.util;

import org.springframework.stereotype.Component;

@Component
public class TemplateResolver {

    private String defaultTemplateId;

    public String resolveDefaultTemplateIdForSignUp() {
        return defaultTemplateId;
    }
}
