package com.beyond.specguard.event.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class KeywordResponse {
    private String type;
    private String keywords;
}
