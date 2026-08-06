package com.example.thexuong.controller.api;

import com.example.thexuong.entity.SystemAuditLog;
import com.example.thexuong.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/audit-logs")
@RequiredArgsConstructor
public class AuditLogRestController {

    private final AuditLogService auditLogService;

    @GetMapping
    public ResponseEntity<?> getAllAuditLogs() {
        try {
            List<SystemAuditLog> logs = auditLogService.getAllAuditLogs();
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", logs
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Lỗi lấy danh sách log: " + e.getMessage()
            ));
        }
    }
}
