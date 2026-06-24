package com.example.thexuong.exception;

import com.example.thexuong.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Bắt tất cả exception từ @RestController và trả về ApiResponse JSON chuẩn.
 * Không cần try-catch trong từng Controller nữa.
 *
 * LƯU Ý: Chỉ áp dụng cho REST Controllers (@RestController).
 * Thymeleaf Controllers (@Controller) vẫn xử lý exception riêng qua RedirectAttributes.
 */
@RestControllerAdvice(basePackages = "com.example.thexuong.controller.api")
public class GlobalExceptionHandler {

    /**
     * 400 — Admin cố tắt chính tài khoản của mình.
     */
    @ExceptionHandler(SelfDeactivationException.class)
    public ResponseEntity<ApiResponse<Void>> handleSelfDeactivation(SelfDeactivationException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage()));
    }

    /**
     * 404 — Không tìm thấy User theo ID hoặc email.
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserNotFound(UserNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage()));
    }

    /**
     * 400 — Tham số không hợp lệ (VD: ID không tồn tại, tên trùng...).
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage()));
    }

    /**
     * 403 — Không có quyền truy cập.
     * LƯU Ý: Spring Security mặc định redirect 403, nhưng khi @RestControllerAdvice
     * bắt được thì nó sẽ trả JSON thay vì redirect — chỉ hoạt động trong REST context.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Bạn không có quyền thực hiện thao tác này."));
    }

    /**
     * 500 — Tất cả lỗi không mong muốn còn lại.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneral(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Đã xảy ra lỗi hệ thống: " + ex.getMessage()));
    }
}
