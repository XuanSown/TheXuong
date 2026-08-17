package com.example.thexuong.dto.loginhistory;

import com.example.thexuong.entity.LoginHistory;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminLoginHistoryResponse {

    private Long id;
    private Long userId;
    private String email;
    private String ipAddress;
    private String userAgent;
    private String provider;
    private Boolean success;
    private String failureReason;
    private LocalDateTime createdAt;

    public static AdminLoginHistoryResponse fromEntity(LoginHistory e) {
        return AdminLoginHistoryResponse.builder()
                .id(e.getId())
                .userId(e.getUserId())
                .email(e.getEmail())
                .ipAddress(e.getIpAddress())
                .userAgent(e.getUserAgent())
                .provider(e.getProvider())
                .success(e.getSuccess())
                .failureReason(e.getFailureReason())
                .createdAt(e.getCreatedAt())
                .build();
    }
}
