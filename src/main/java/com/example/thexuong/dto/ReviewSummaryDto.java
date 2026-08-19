package com.example.thexuong.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewSummaryDto {
    private double averageRating;
    private long totalCount;
    private Map<Integer, Long> distribution; // key 5 -> 1
}
