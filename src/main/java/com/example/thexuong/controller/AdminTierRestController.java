package com.example.thexuong.controller;

import com.example.thexuong.dto.ApiResponse;
import com.example.thexuong.entity.PointTier;
import com.example.thexuong.service.PointTierService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/tiers")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ADMIN', 'BOTH')")
public class AdminTierRestController {

    private final PointTierService pointTierService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PointTier>>> getAllTiers() {
        List<PointTier> tiers = pointTierService.getAllTiers();
        return ResponseEntity.ok(ApiResponse.ok("Lấy danh sách cấp bậc thành công", tiers));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PointTier>> createTier(@RequestBody PointTier pointTier) {
        try {
            PointTier created = pointTierService.createTier(pointTier);
            return ResponseEntity.ok(ApiResponse.ok("Tạo cấp bậc thành công", created));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PointTier>> updateTier(@PathVariable Long id, @RequestBody PointTier pointTier) {
        try {
            PointTier updated = pointTierService.updateTier(id, pointTier);
            return ResponseEntity.ok(ApiResponse.ok("Cập nhật cấp bậc thành công", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTier(@PathVariable Long id) {
        try {
            pointTierService.deleteTier(id);
            return ResponseEntity.ok(ApiResponse.ok("Xóa cấp bậc thành công", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
