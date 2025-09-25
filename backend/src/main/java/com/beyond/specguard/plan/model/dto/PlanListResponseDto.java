package com.beyond.specguard.plan.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PlanListResponseDto {
    @JsonProperty("contents")
    private List<PlanResponseDto> responseDtos;
    private long totalElements;
    private int totalPages;
    private int currentPage;
    private int size;
}
