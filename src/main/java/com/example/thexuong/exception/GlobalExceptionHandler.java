package com.example.thexuong.exception;

import com.example.thexuong.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Bắt tất cả exception từ @RestController và trả về ApiResponse JSON chuẩn.
 * Không cần try-catch trong từng Controller nữa.
 */
@RestControllerAdvice(basePackages = {"com.example.thexuong.controller.api", "com.example.thexuong.controller"})
@Slf4j
public class GlobalExceptionHandler {

  /**
   * 400 — Validation failed (@Valid annotation)
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
    String message = ex.getBindingResult().getFieldErrors().stream()
      .findFirst()
      .map(err -> err.getDefaultMessage())
      .orElse("Dữ liệu không hợp lệ");
    return ResponseEntity
      .status(HttpStatus.BAD_REQUEST)
      .body(ApiResponse.error(message));
  }

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
   * 404 — FAQ không tồn tại (Admin Customer Care).
   */
  @ExceptionHandler(FaqNotFoundException.class)
  public ResponseEntity<ApiResponse<Void>> handleFaqNotFound(FaqNotFoundException ex) {
    return ResponseEntity
      .status(HttpStatus.NOT_FOUND)
      .body(ApiResponse.error(ex.getMessage()));
  }

  /**
   * 400 — User cố spend/reverse nhiều điểm hơn số dư (Batch 1 Loyalty).
   */
  @ExceptionHandler(PointBalanceException.class)
  public ResponseEntity<ApiResponse<Void>> handlePointBalance(PointBalanceException ex) {
    return ResponseEntity
      .status(HttpStatus.BAD_REQUEST)
      .body(ApiResponse.error(ex.getMessage()));
  }

  /**
   * 409 — State machine violation (Batch 0 OrderStatus).
   * Ví dụ: cố chuyển COMPLETED → SHIPPING, hoặc PENDING → COMPLETED.
   */
  @ExceptionHandler(IllegalOrderTransitionException.class)
  public ResponseEntity<ApiResponse<Void>> handleIllegalTransition(IllegalOrderTransitionException ex) {
    return ResponseEntity
      .status(HttpStatus.CONFLICT)
      .body(ApiResponse.error(ex.getMessage()));
  }

  /**
   * 400 — Voucher không hợp lệ (hết hạn, đã dùng, sai điều kiện) (Batch 2).
   */
  @ExceptionHandler(VoucherInvalidException.class)
  public ResponseEntity<ApiResponse<Void>> handleVoucherInvalid(VoucherInvalidException ex) {
    return ResponseEntity
      .status(HttpStatus.BAD_REQUEST)
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
   * 423 — Tài khoản bị khóa (DisabledException khi đăng nhập).
   * Handler riêng để phân biệt với 401 sai email/mật khẩu.
   */
  @ExceptionHandler(DisabledException.class)
  public ResponseEntity<ApiResponse<Void>> handleDisabledAccount(DisabledException ex) {
    return ResponseEntity
      .status(HttpStatus.LOCKED)
      .body(ApiResponse.error("Tài khoản của bạn đã bị khóa. Vui lòng liên hệ quản trị viên."));
  }

  /**
   * 401 — Xác thực thất bại (sai email/mật khẩu). Bắt cho controller gọi
   * authenticationManager.authenticate() thủ công (VD: AuthRestController.login)
   * thay vì đi qua Spring Security filter chain — tránh rơi vào catch-all 500.
   */
  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(AuthenticationException ex) {
    return ResponseEntity
      .status(HttpStatus.UNAUTHORIZED)
      .body(ApiResponse.error("Email hoặc mật khẩu không đúng"));
  }

  /**
   * 404 — Review (hoặc Product khi tạo review) không tồn tại.
   */
  @ExceptionHandler(ReviewNotFoundException.class)
  public ResponseEntity<ApiResponse<Void>> handleReviewNotFound(ReviewNotFoundException ex) {
    return ResponseEntity
      .status(HttpStatus.NOT_FOUND)
      .body(ApiResponse.error(ex.getMessage()));
  }

  /**
   * 403 — Review: chưa mua sản phẩm / không phải chủ review / không phải admin.
   */
  @ExceptionHandler(ReviewNotAllowedException.class)
  public ResponseEntity<ApiResponse<Void>> handleReviewNotAllowed(ReviewNotAllowedException ex) {
    return ResponseEntity
      .status(HttpStatus.FORBIDDEN)
      .body(ApiResponse.error(ex.getMessage()));
  }

  /**
   * 409 — User đã review sản phẩm này rồi.
   */
  @ExceptionHandler(ReviewAlreadyExistsException.class)
  public ResponseEntity<ApiResponse<Void>> handleReviewAlreadyExists(ReviewAlreadyExistsException ex) {
    return ResponseEntity
      .status(HttpStatus.CONFLICT)
      .body(ApiResponse.error(ex.getMessage()));
  }

  /**
   * 409 — Hết hàng / không đủ tồn kho.
   */
  @ExceptionHandler(InsufficientStockException.class)
  public ResponseEntity<ApiResponse<Void>> handleInsufficientStock(InsufficientStockException ex) {
    return ResponseEntity
      .status(HttpStatus.CONFLICT)
      .body(ApiResponse.error(ex.getMessage()));
  }

  /**
   * 500 — Tất cả lỗi không mong muốn còn lại.
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Void>> handleGeneral(Exception ex) {
    log.error("Unexpected error occurred: {}", ex.getMessage(), ex);
    return ResponseEntity
      .status(HttpStatus.INTERNAL_SERVER_ERROR)
      .body(ApiResponse.error("Đã xảy ra lỗi hệ thống. Vui lòng thử lại sau."));
  }
}
