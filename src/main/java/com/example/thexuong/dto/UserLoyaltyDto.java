package com.example.thexuong.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class UserLoyaltyDto {
    private Long userId;
    private String currentTierCode;
    private String currentTierName;
    private Integer currentPoints;
    private BigDecimal totalSpent365Days;
    private Integer totalPointsEarned365Days;
    private String nextTierCode;
    private String nextTierName;
    private BigDecimal minSpentNextTier;
    private Integer minPointsNextTier;
    private BigDecimal spentRemainingToNextTier;
    private Integer pointsRemainingToNextTier;
}
