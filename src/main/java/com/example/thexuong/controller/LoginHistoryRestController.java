package com.example.thexuong.controller;

import com.example.thexuong.dto.ApiResponse;
import com.example.thexuong.dto.customercare.PageResponse;
import com.example.thexuong.dto.loginhistory.AdminLoginHistoryResponse;
import com.example.thexuong.service.LoginHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/login-history")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ADMIN', 'BOTH')")
public class LoginHistoryRestController {

    private final LoginHistoryService loginHistoryService;

    /**
     * GET /api/v1/admin/login-history?email=&provider=&success=&from=&to=&page=&size=&sort=
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AdminLoginHistoryResponse>>> getHistory(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String provider,
            @RequestParam(required = false) Boolean success,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        return ResponseEntity.ok(ApiResponse.ok("Lấy lịch sử đăng nhập thành công.",
                loginHistoryService.getHistory(email, provider, success, from, to, page, size, sort)));
    }
}
