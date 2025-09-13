package com.beyond.specguard.plan.model.dto;

import lombok.*;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PlanListResponseDto {
    private List<PlanResponseDto> responseDtos;
    private long totalElements;
    private int totalPages;
    private int currentPage;
    private int size;
}
