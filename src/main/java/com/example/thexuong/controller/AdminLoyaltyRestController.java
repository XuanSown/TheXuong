package com.example.thexuong.controller;

import com.example.thexuong.dto.ApiResponse;
import com.example.thexuong.dto.UserLoyaltyDto;
import com.example.thexuong.service.PointTierService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/loyalty/users")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ADMIN', 'BOTH')")
public class AdminLoyaltyRestController {

    private final PointTierService pointTierService;

    @GetMapping("/{userId}/progress")
    public ResponseEntity<ApiResponse<UserLoyaltyDto>> getLoyaltyProgress(@PathVariable Long userId) {
        UserLoyaltyDto progress = pointTierService.getLoyaltyProgress(userId);
        if (progress == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Không tìm thấy thông tin loyalty cho user này"));
        }
        return ResponseEntity.ok(ApiResponse.ok("Lấy thông tin loyalty thành công", progress));
    }
}
