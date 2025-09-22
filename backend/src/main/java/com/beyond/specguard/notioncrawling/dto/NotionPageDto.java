package com.beyond.specguard.notioncrawling.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

public class NotionPageDto {
    private final String url;
    private final String title;
    private final String content;
    private final List<String> codeBlocks;
    private final List<String> tags;

    public NotionPageDto(String url, String title, String content,
                         List<String> codeBlocks, List<String> tags) {
        this.url = url;
        this.title = title;
        this.content = content;
        this.codeBlocks = codeBlocks;
        this.tags = tags;
    }

    public String getUrl() { return url; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public List<String> getCodeBlocks() { return codeBlocks; }
    public List<String> getTags() { return tags; }

    @Override
    public String toString() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (Exception e) {
            return "NotionPageDto{error converting to JSON}";
        }
    }
}
