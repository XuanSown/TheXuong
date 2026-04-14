package com.example.thexuong.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Format JSON thống nhất cho mọi REST API response.
 * Generic type T cho phép data là bất kỳ kiểu gì (DTO, List, null).
 *
 * Ví dụ response thành công:
 * { "success": true, "message": "Cập nhật thành công", "data": { ... } }
 *
 * Ví dụ response lỗi:
 * { "success": false, "message": "Không thể tự khóa tài khoản.", "data": null }
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;

    // ==================== Factory Methods ====================

    public static <T> ApiResponse<T> ok(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> ok(String message) {
        return ok(message, null);
    }

    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .data(null)
                .build();
    }
}
