package com.example.thexuong.dto.customercare;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Response cho GET /api/v1/admin/customer-care/overview.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerCareOverviewResponse {
    private long totalFaqs;
    private long totalConversations;
    private long todayMessages;
    private String topIntent;
}
